package org.example.dockernetwork.Service.Impl;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.ContainerNetwork;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import org.example.dockernetwork.Service.BirdService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

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
    public ResponseEntity<String> configure(String containerName){
        try {
            // 1. 获取容器信息
            InspectContainerResponse containerInfo = dockerClient.inspectContainerCmd(containerName).exec();
            Map<String, ContainerNetwork> networks = containerInfo.getNetworkSettings().getNetworks();

            // 2. 构造 bird.conf
            StringBuilder interfacesBuilder = new StringBuilder();
            String routerIp = null;
            int ethIndex = 0;

            for (Map.Entry<String, ContainerNetwork> entry : networks.entrySet()) {
                String ip = entry.getValue().getIpAddress();
                if (ip == null || ip.isEmpty()) continue;

                if (routerIp == null) routerIp = ip;

                interfacesBuilder.append("        interface \"eth")
                        .append(ethIndex)
                        .append("\" {\n")
                        .append("            type broadcast;\n")
                        .append("            hello 5;\n")
                        .append("        };\n");

                ethIndex++;
            }

            if (routerIp == null) {
                return ResponseEntity.badRequest().body("未找到容器的有效 IP 地址");
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

protocol ospf {
    area 0 {
%s
    };
}
                    """, routerIp, interfacesBuilder.toString());

            // 3. 写入临时文件
            Path tempFile = Files.createTempFile("bird", ".conf");
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

            return ResponseEntity.ok("容器 " + containerName + " 的 BIRD 配置已更新并重载");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("配置失败：" + e.getMessage());
        }
    }
}
