package org.example.dockernetwork.Service.Impl;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.ContainerNetwork;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import org.example.dockernetwork.Entity.StaticRouteEntity;
import org.example.dockernetwork.Service.BirdService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class BirdServiceImpl implements BirdService {

    private final DockerClient dockerClient;

    public BirdServiceImpl() {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("unix:///var/run/docker.sock")
                .build();

        this.dockerClient = DockerClientBuilder.getInstance(config)
                .withDockerHttpClient(new ApacheDockerHttpClient.Builder()
                        .dockerHost(config.getDockerHost())
                        .build())
                .build();
    }

    @Override
    public ResponseEntity<String> activeConfigure(String containerName) {
        Path tempFile = null;
        try {

            // 2. 构造 bird.conf
            StringBuilder interfacesBuilder = new StringBuilder();
            String routerIp = null;

            // 解析接口名称
            List<String> interfaceNames = getInterfaces(containerName);

            for (String interfaceName : interfaceNames) {
                // 3. 假设每个接口都有对应的 IP 地址，可以通过另一个命令获取

                String ip = getIpForInterface(containerName, interfaceName);

                if (ip == null || ip.isEmpty()) continue;

                if (routerIp == null) routerIp = ip;

                // 构建 bird 配置
                interfacesBuilder.append("        interface \"")
                        .append(interfaceName) // 使用真实的接口名
                        .append("\" {\n")
                        .append("            type broadcast;\n")
                        .append("            hello 5;\n")
                        .append("        };\n");
            }

            if (routerIp == null) {
                return ResponseEntity.badRequest().body("未找到容器的有效 IP 地址");
            }

            // 构造 bird.conf 配置
            String birdConf = String.format(""" 
router id %s;

protocol kernel {
    scan time 60;
    import none;
    export all;
}

protocol device {
    scan time 60;
}

protocol ospf {
    area 0 {
%s
    };
}
                        """, routerIp, interfacesBuilder.toString());

            // 3. 写入临时文件
            tempFile = Files.createTempFile("bird", ".conf");
            Files.writeString(tempFile, birdConf);

            // 4. 拷贝到容器：/etc/bird/bird.conf
            ProcessBuilder pb = new ProcessBuilder(
                    "docker", "cp",
                    tempFile.toAbsolutePath().toString(),
                    containerName + ":/etc/bird/bird.conf"
            );
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                return ResponseEntity.status(500).body("拷贝配置文件进容器失败");
            }

            // 5. 执行 birdc configure
            ExecCreateCmdResponse exec = dockerClient.execCreateCmd(containerName)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .withCmd("birdc", "configure")
                    .exec();

            dockerClient.execStartCmd(exec.getId()).start().awaitCompletion();
            Files.deleteIfExists(tempFile);  // 删除临时文件
            return ResponseEntity.ok("容器 " + containerName + " 的 BIRD 配置已更新并重载");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("配置失败：" + e.getMessage());
        } finally {
            // 确保临时文件被删除
            if (tempFile != null && Files.exists(tempFile)) {
                try {
                    Files.delete(tempFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public ResponseEntity<String> staticConfigure(String containerName, List<StaticRouteEntity> routeList) {
        try {
            // 校验 IP 格式
            for (StaticRouteEntity route : routeList) {
                String targetIp = route.getTargetIp();
                String nextHopIp = route.getNextHopIp();

                if (!targetIp.matches("\\d+\\.\\d+\\.\\d+\\.\\d+(/\\d+)?") || !nextHopIp.matches("\\d+\\.\\d+\\.\\d+\\.\\d+")) {
                    return ResponseEntity.badRequest().body("IP 地址格式错误: " + targetIp + " -> " + nextHopIp);
                }
            }

            // 1. 获取容器信息
            InspectContainerResponse containerInfo = dockerClient.inspectContainerCmd(containerName).exec();
            Map<String, ContainerNetwork> networks = containerInfo.getNetworkSettings().getNetworks();
            Map.Entry<String, ContainerNetwork> firstEntry = networks.entrySet().iterator().next();
            String routerIp = firstEntry.getValue().getIpAddress();

            if (routerIp == null) {
                return ResponseEntity.badRequest().body("未找到容器的有效 IP 地址");
            }

            // 2. 构造 bird.conf 内容
            StringBuilder staticRoutes = new StringBuilder();
            for (StaticRouteEntity route : routeList) {
                staticRoutes.append(String.format("    route %s via %s;\n", route.getTargetIp(), route.getNextHopIp()));
            }

            String birdConf = String.format("""
router id %s;

protocol kernel {
    scan time 60;
    import none;
    export all;
}

protocol device {
    scan time 60;
}

protocol static {
%s
}
        """, routerIp, staticRoutes.toString());

            // 3. 写入临时文件
            Path tempFile = Files.createTempFile("bird", ".conf");
            Files.writeString(tempFile, birdConf);

            // 4. 拷贝配置文件进容器
            ProcessBuilder pb = new ProcessBuilder(
                    "docker", "cp",
                    tempFile.toAbsolutePath().toString(),
                    containerName + ":/etc/bird/bird.conf"
            );
            if (pb.start().waitFor() != 0) {
                return ResponseEntity.status(500).body("拷贝配置文件失败");
            }

            // 5. 执行 birdc configure
            ExecCreateCmdResponse exec = dockerClient.execCreateCmd(containerName)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .withCmd("birdc", "configure")
                    .exec();

            dockerClient.execStartCmd(exec.getId()).start().awaitCompletion();
            Files.deleteIfExists(tempFile);

            return ResponseEntity.ok("容器 " + containerName + " 的 BIRD 静态路由已成功配置");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("配置失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<String> addStaticRoutes(String containerName, List<StaticRouteEntity> staticRoutes) {
        try {
            // 1. 获取容器信息
            InspectContainerResponse containerInfo = dockerClient.inspectContainerCmd(containerName).exec();
            Map<String, ContainerNetwork> networks = containerInfo.getNetworkSettings().getNetworks();

            // 2. 获取容器的 IP 地址
            Map.Entry<String, ContainerNetwork> firstEntry = networks.entrySet().iterator().next();
            ContainerNetwork firstNetwork = firstEntry.getValue();
            String routerIp = firstNetwork.getIpAddress();

            if (routerIp == null) {
                return ResponseEntity.badRequest().body("未找到容器的有效 IP 地址");
            }

            // 3. 将容器内的 bird.conf 文件复制到本地临时文件
            String birdConfPath = "/etc/bird/bird.conf";
            Path tempFile = Files.createTempFile("bird", ".conf");
            ProcessBuilder cpProcess = new ProcessBuilder(
                    "docker", "cp",
                    containerName + ":" + birdConfPath,
                    tempFile.toAbsolutePath().toString()
            );
            Process cpProcessInstance = cpProcess.start();
            int cpExitCode = cpProcessInstance.waitFor();

            if (cpExitCode != 0) {
                return ResponseEntity.status(500).body("拷贝配置文件失败");
            }

            // 4. 读取临时文件中的配置内容
            String birdConfContent = Files.readString(tempFile, StandardCharsets.UTF_8);

            List<String> existingRoutes = new ArrayList<>();
            StringBuilder birdConfUpdatedContent = new StringBuilder();
            boolean staticSectionFound = false;

            // 5. 解析现有的静态路由部分
            for (String line : birdConfContent.split("\n")) {
                // 忽略注释行
                if (line.trim().startsWith("#")) {
                    continue;
                }
                // 如果找到静态路由部分，则提取现有的路由
                if (line.trim().startsWith("protocol static {")) {
                    staticSectionFound = true;
                }
                else if (staticSectionFound && line.trim().startsWith("}")) {
                    break;  // 结束静态路由的读取
                }
                else if (staticSectionFound) {
                    // 记录现有静态路由
                    existingRoutes.add("    "+line.trim());
                }
            }

            // 日志：打印现有路由
            System.out.println("现有的静态路由：");
            existingRoutes.forEach(System.out::println);

            // 6. 合并现有路由和新路由
            for (StaticRouteEntity route : staticRoutes) {
                String routeLine = String.format("    route %s via %s;", route.getTargetIp(), route.getNextHopIp());
                existingRoutes.add(routeLine);  // 将新路由添加到现有路由后
            }

            // 日志：打印合并后的静态路由
            System.out.println("合并后的静态路由：");
            existingRoutes.forEach(System.out::println);

            // 7. 更新静态路由部分
            birdConfUpdatedContent.append("protocol static {\n");
            for (String route : existingRoutes) {
                birdConfUpdatedContent.append(route).append("\n");
            }
            birdConfUpdatedContent.append("}\n");

            // 8. 构造新的 bird.conf 文件内容
            birdConfUpdatedContent.insert(0, String.format("""
router id %s;

protocol kernel {
    scan time 60;
    import none;
    export all;
}

protocol device {
    scan time 60;
}

""", routerIp));

            // 9. 写入更新后的临时文件
            Files.writeString(tempFile, birdConfUpdatedContent.toString());

            // 10. 拷贝更新后的配置文件到容器：/etc/bird/bird.conf
            ProcessBuilder pb = new ProcessBuilder(
                    "docker", "cp",
                    tempFile.toAbsolutePath().toString(),
                    containerName + ":/etc/bird/bird.conf"
            );
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                return ResponseEntity.status(500).body("拷贝配置文件进容器失败");
            }

            // 11. 执行 birdc configure 命令重新加载配置
            ExecCreateCmdResponse exec = dockerClient.execCreateCmd(containerName)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .withCmd("birdc", "configure")
                    .exec();

            dockerClient.execStartCmd(exec.getId()).start().awaitCompletion();
            Files.deleteIfExists(tempFile);

            return ResponseEntity.ok("静态路由已成功添加并配置");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("配置失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<String> removeStaticRoutes(String containerName, List<StaticRouteEntity> staticRoutes) {
        try {
            // 构造要删除的静态路由格式
            Set<String> routesToDelete = staticRoutes.stream()
                    .map(route -> String.format("route %s via %s;", route.getTargetIp().trim(), route.getNextHopIp().trim()))
                    .map(String::trim)
                    .collect(Collectors.toSet());

            // 获取容器 IP
            InspectContainerResponse containerInfo = dockerClient.inspectContainerCmd(containerName).exec();
            Map<String, ContainerNetwork> networks = containerInfo.getNetworkSettings().getNetworks();
            String routerIp = networks.values().iterator().next().getIpAddress();

            if (routerIp == null) {
                return ResponseEntity.badRequest().body("未找到容器的有效 IP 地址");
            }

            // 拷贝 bird.conf 文件到临时文件
            Path tempFile = Files.createTempFile("bird", ".conf");
            ProcessBuilder cpProcess = new ProcessBuilder("docker", "cp",
                    containerName + ":/etc/bird/bird.conf",
                    tempFile.toAbsolutePath().toString());
            int cpExit = cpProcess.start().waitFor();
            if (cpExit != 0) {
                return ResponseEntity.status(500).body("拷贝配置文件失败");
            }

            // 读取配置内容
            String birdConfContent = Files.readString(tempFile, StandardCharsets.UTF_8);

            // 解析现有静态路由
            List<String> existingRoutes = new ArrayList<>();
            boolean staticSectionFound = false;
            for (String line : birdConfContent.split("\n")) {
                if (line.trim().startsWith("#")) continue;
                if (line.trim().startsWith("protocol static {")) {
                    staticSectionFound = true;
                    continue;
                }
                if (staticSectionFound && line.trim().startsWith("}")) break;
                if (staticSectionFound) {
                    existingRoutes.add("    " + line.trim());
                }
            }

            // ✅ 保留：打印原始路由
            System.out.println("原始静态路由配置：");
            existingRoutes.forEach(System.out::println);

            // 删除目标路由
            List<String> updatedRoutes = existingRoutes.stream()
                    .filter(r -> !routesToDelete.contains(r.trim()))
                    .collect(Collectors.toList());

            // ✅ 保留：打印更新后的路由
            System.out.println("更新后的静态路由配置：");
            updatedRoutes.forEach(System.out::println);

            // 重写 bird.conf 文件内容
            StringBuilder birdConfUpdatedContent = new StringBuilder();
            birdConfUpdatedContent.append("protocol static {\n");
            for (String route : updatedRoutes) {
                birdConfUpdatedContent.append(route).append("\n");
            }
            birdConfUpdatedContent.append("}\n");

            birdConfUpdatedContent.insert(0, String.format("""
router id %s;

protocol kernel {
    scan time 60;
    import none;
    export all;
}

protocol device {
    scan time 60;
}

""", routerIp));

            Files.writeString(tempFile, birdConfUpdatedContent.toString());

            // 拷贝回容器
            ProcessBuilder cpBack = new ProcessBuilder("docker", "cp",
                    tempFile.toAbsolutePath().toString(),
                    containerName + ":/etc/bird/bird.conf");
            int cpBackExit = cpBack.start().waitFor();
            if (cpBackExit != 0) {
                return ResponseEntity.status(500).body("拷贝配置文件进容器失败");
            }

            // 执行 birdc configure
            ExecCreateCmdResponse exec = dockerClient.execCreateCmd(containerName)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .withCmd("birdc", "configure")
                    .exec();
            dockerClient.execStartCmd(exec.getId()).start().awaitCompletion();
            Files.deleteIfExists(tempFile);

            return ResponseEntity.ok("静态路由已成功删除并配置");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("配置失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<String> resetBirdConfig(String containerName) {
        try {
            // 获取容器 IP 地址
            InspectContainerResponse containerInfo = dockerClient.inspectContainerCmd(containerName).exec();
            Map<String, ContainerNetwork> networks = containerInfo.getNetworkSettings().getNetworks();
            String routerIp = networks.values().iterator().next().getIpAddress();

            if (routerIp == null) {
                return ResponseEntity.badRequest().body("未找到容器的有效 IP 地址");
            }

            // 生成最简配置内容
            String resetConf = String.format("""
router id %s;

protocol kernel {
    scan time 60;
    import none;
    export all;
}

protocol device {
    scan time 60;
}

protocol static {
}
""", routerIp);

            // 写入临时文件
            Path tempFile = Files.createTempFile("bird_reset", ".conf");
            Files.writeString(tempFile, resetConf, StandardCharsets.UTF_8);

            // 拷贝回容器
            ProcessBuilder cpBack = new ProcessBuilder("docker", "cp",
                    tempFile.toAbsolutePath().toString(),
                    containerName + ":/etc/bird/bird.conf");
            int cpBackExit = cpBack.start().waitFor();
            if (cpBackExit != 0) {
                return ResponseEntity.status(500).body("拷贝配置文件进容器失败");
            }

            // 执行 birdc configure
            ExecCreateCmdResponse exec = dockerClient.execCreateCmd(containerName)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .withCmd("birdc", "configure")
                    .exec();
            dockerClient.execStartCmd(exec.getId()).start().awaitCompletion();

            Files.deleteIfExists(tempFile);

            return ResponseEntity.ok("BIRD 配置已成功重置");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("配置重置失败：" + e.getMessage());
        }
    }

    @Override
    public List<StaticRouteEntity> getStaticRoutes(String containerName) {
        List<StaticRouteEntity> routeList = new ArrayList<>();

        try {
            // 创建临时文件用于读取 bird.conf
            Path tempFile = Files.createTempFile("bird_read", ".conf");

            // 拷贝 bird.conf 从容器到主机
            ProcessBuilder cpProcess = new ProcessBuilder("docker", "cp",
                    containerName + ":/etc/bird/bird.conf",
                    tempFile.toAbsolutePath().toString());
            int cpExit = cpProcess.start().waitFor();
            if (cpExit != 0) {
                return routeList; // 返回空列表，表示失败
            }

            // 读取配置文件内容
            String birdConfContent = Files.readString(tempFile, StandardCharsets.UTF_8);
            Files.deleteIfExists(tempFile);

            // 解析静态路由条目
            boolean inStaticSection = false;
            for (String line : birdConfContent.split("\n")) {
                line = line.trim();
                if (line.startsWith("protocol static {")) {
                    inStaticSection = true;
                    continue;
                }
                if (inStaticSection && line.startsWith("}")) break;

                if (inStaticSection && line.startsWith("route ")) {
                    Pattern pattern = Pattern.compile("route\\s+(\\S+)\\s+via\\s+(\\S+);");
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        String targetIp = matcher.group(1);
                        String nextHopIp = matcher.group(2);
                        routeList.add(new StaticRouteEntity(targetIp, nextHopIp));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return routeList;
    }

    private List<String> getInterfaces(String container) {
        List<String> interfaces = new ArrayList<>();
        String command = String.format("docker exec %s bash -c \"ls /sys/class/net\"", container);
        try {
            Process process = new ProcessBuilder("bash", "-c", command).start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                // 排除 lo（本地回环接口）
                if (!line.trim().equals("lo")) {
                    interfaces.add(line.trim());
                }
            }
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return interfaces;
    }

     private String getIpForInterface(String containerName, String interfaceName) throws IOException, InterruptedException {
        // 使用 docker exec 执行 ip a 命令
        ProcessBuilder pb = new ProcessBuilder(
                "docker", "exec", containerName, "ip", "a", "show", interfaceName
        );
        Process process = pb.start();
        int exitCode = process.waitFor();
        if (exitCode == 0) {
            String output = new String(process.getInputStream().readAllBytes());
            // 解析输出，获取 IP 地址
            String[] lines = output.split("\n");
            for (String line : lines) {
                if (line.trim().startsWith("inet")) {
                    String ip = line.trim().split(" ")[1].split("/")[0];
                    return ip;
                }
            }
        }
        return null;
    }

}
