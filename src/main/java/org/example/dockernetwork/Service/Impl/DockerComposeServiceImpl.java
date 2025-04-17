package org.example.dockernetwork.Service.Impl;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import org.example.dockernetwork.Entity.DockerContainer;
import org.example.dockernetwork.Entity.DockerNetwork;
import org.example.dockernetwork.Service.DockerComposeService;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class DockerComposeServiceImpl implements DockerComposeService {

    private final DockerClient dockerClient;

    public DockerComposeServiceImpl() {
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
    public void create(List<DockerNetwork> dockerNetworkList, List<DockerContainer> dockerContainerList, String file) {
        Set<String> allNetworkSet = new HashSet<>();
        Set<String> newNetworkSet = new HashSet<>();

        for (DockerNetwork dockerNetwork : dockerNetworkList) {
            System.out.println("Driver: " + dockerNetwork.getDriver());
            System.out.println("Name: " + dockerNetwork.getName());
            System.out.println("Subnet: " + dockerNetwork.getSubnet());
            System.out.println("Gateway: " + dockerNetwork.getGateway());
            System.out.println("----------");
            newNetworkSet.add(dockerNetwork.getName());
        }

        for (DockerContainer dockerContainer : dockerContainerList) {
            System.out.println("Image: " + dockerContainer.getImage());
            System.out.println("Name: " + dockerContainer.getName());
            System.out.println("Networks: " + String.join(", ", dockerContainer.getNetworks()));
            System.out.println("----------");
            allNetworkSet.addAll(dockerContainer.getNetworks());
        }

        allNetworkSet.removeAll(newNetworkSet);
        List<String> oldNetworkList = new ArrayList<>(allNetworkSet);
        try {
            generateFile(dockerContainerList, dockerNetworkList, oldNetworkList);
            executeCommand(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void generateFile(List<DockerContainer> dockerContainers, List<DockerNetwork> dockerNetworks, List<String> oldNetworks) throws IOException {
        File file = new File("docker-compose.yml");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("version: '3'\n\n");
            writer.write("services:\n");

            for(DockerContainer dockerContainer : dockerContainers){
                writer.write("  " + dockerContainer.getName() + ":\n");
                writer.write("    image: " + dockerContainer.getImage() + "\n");
                writer.write("    container_name: " + dockerContainer.getName() + "\n");
                writer.write("    networks:\n");
                for(String network: dockerContainer.getNetworks()){
                    writer.write("      " + network + ":\n");
                }
                writer.write("    sysctls:\n");
                writer.write("      - net.ipv4.ip_forward=1\n");
                writer.write("    cap_add:\n");
                writer.write("      - NET_ADMIN\n\n");
            }

            // 创建网络配置
            writer.write("networks:\n");
            for(DockerNetwork dockerNetwork : dockerNetworks){
                writer.write("  " + dockerNetwork.getName() + ":\n");
                writer.write("    driver: " + dockerNetwork.getDriver() + "\n");
                writer.write("    ipam:\n");
                writer.write("      config:\n");
                writer.write("        - subnet: " + dockerNetwork.getSubnet() + "\n");
                writer.write("          gateway: " + dockerNetwork.getGateway() + "\n");

            }
            for(String network: oldNetworks){
                writer.write("  " + network + ":\n");
                writer.write("    external: true\n");
            }
        }
    }


    public void executeCommand(String filePath) {
        // 使用 ProcessBuilder 来执行系统命令
        ProcessBuilder processBuilder = new ProcessBuilder();
        // 使用 -d 参数让 docker-compose 在后台运行
        processBuilder.command("docker-compose", "-f", filePath, "up", "-d");

        try {
            // 启动进程
            Process process = processBuilder.start();

            // 获取输出流并打印到控制台
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // 等待进程结束并获取退出代码
            int exitCode = process.waitFor();
            System.out.println("docker-compose 执行完毕，退出代码：" + exitCode);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
