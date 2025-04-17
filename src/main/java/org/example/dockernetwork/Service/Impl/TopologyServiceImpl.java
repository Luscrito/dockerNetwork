package org.example.dockernetwork.Service.Impl;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Network;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import org.example.dockernetwork.Service.TopologyService;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TopologyServiceImpl implements TopologyService {

    private final DockerClient dockerClient;

    public TopologyServiceImpl() {
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
    public String create(String file1, String file2) {
        String command = "dot -Tpng "+file1+" -o "+file2;
        System.out.println(command);
        createDotFile();

        try {
            // 执行命令
            Process process;
            try {
                process = Runtime.getRuntime().exec(command);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // 等待命令执行完成
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                return "成功生成 PNG 图像：topology.png";
            }
            else {
                return "命令执行失败，退出码：" + exitCode;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return "执行出错";
        }
    }

    @Override
    public String delete(String file1, String file2) {

        Path path1 = Paths.get(file1);
        Path path2 = Paths.get(file2);

        try {
            Files.delete(path1);
            Files.delete(path2);
            return "文件删除成功";
        } catch (IOException e) {
            return "删除文件时出错: " + e.getMessage();
        }
    }

    public Map<String, List<String>> getInfo() {
        Map<String,List<String>> NetInfo = new HashMap<>();

        try {
            // 获取所有Docker网络
            List<Network> networks = dockerClient.listNetworksCmd().exec();
            for (Network network : networks) {
                List<String> NetContainers = new ArrayList<>();
                String networkId = network.getId();

                // 获取网络详细信息（包含关联的容器）
                Map<String, Network.ContainerNetworkConfig> containers = dockerClient.inspectNetworkCmd()
                        .withNetworkId(networkId)
                        .exec()
                        .getContainers();

                // 提取容器信息
                if (containers != null && !containers.isEmpty()) {
                    for (Map.Entry<String, Network.ContainerNetworkConfig> entry : containers.entrySet()) {
                        Network.ContainerNetworkConfig containerSettings = entry.getValue();
                        NetContainers.add(containerSettings.getName());
                    }
                } else {
                    System.out.println("  没有容器连接到这个网络。");
                }
                NetInfo.put(network.getName(),NetContainers);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return NetInfo;
    }

    public void createDotFile() {
        // 创建一个文件对象，指定文件名
        Map<String, List<String>> NetInfo = getInfo();
        File file = new File("topology.dot");

        // 使用 try-with-resources 确保文件操作完成后自动关闭
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // 向文件中写入数据
            writer.write("graph G {");
            writer.newLine();
            NetInfo.forEach((key,list)->
                    {
                        if(list.size() == 1){
                            try {
                                writer.write("  "+list.get(0)+"\n");
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        else {
                            for (int i = 0; i < list.size() - 1; i++) {
                                for (int j = i + 1; j < list.size(); j++) {
                                    try {
                                        writer.write("  " + list.get(i) + " -- " + list.get(j)+"\n");
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }

                                }
                            }
                        }
                    }
            );
            writer.write("}");
            writer.newLine();

            System.out.println("文件 topology.dot 已成功创建并写入数据。");

        } catch (IOException e) {
            // 捕获并处理文件写入过程中可能发生的异常
            e.printStackTrace();
        }
    }
}
