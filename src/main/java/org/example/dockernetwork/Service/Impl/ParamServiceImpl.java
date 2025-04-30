package org.example.dockernetwork.Service.Impl;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.UpdateContainerCmd;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import org.example.dockernetwork.Service.ParamService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class ParamServiceImpl implements ParamService {

    private final DockerClient dockerClient;

    public ParamServiceImpl() {
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
    public void setDelay(String container, int delayInMs) {
        for (String iface : getInterfaces(container)) {
            String deleteCommand = String.format("docker exec %s tc qdisc del dev %s root", container, iface);
            String addCommand = String.format("docker exec %s tc qdisc add dev %s root netem delay %dms", container, iface, delayInMs);
            executeCommand(deleteCommand);
            executeCommand(addCommand);
        }
    }

    @Override
    public void setPacketLoss(String container, int lossPercentage) {
        for (String iface : getInterfaces(container)) {
            String deleteCommand = String.format("docker exec %s tc qdisc del dev %s root", container, iface);
            String addCommand = String.format("docker exec %s tc qdisc add dev %s root netem loss %d%%", container, iface, lossPercentage);
            executeCommand(deleteCommand);
            executeCommand(addCommand);
        }
    }

    @Override
    public void setBandwidth(String container, String bandwidthLimit, String burst, int latency) {
        for (String iface : getInterfaces(container)) {
            String deleteCommand = String.format("docker exec %s tc qdisc del dev %s root", container, iface);
            String addCommand = String.format(
                    "docker exec %s tc qdisc add dev %s root tbf rate %sbit burst %sbit latency %dms",
                    container, iface, bandwidthLimit, burst, latency);
            executeCommand(deleteCommand);
            executeCommand(addCommand);
        }
    }

    @Override
    public void setProperties(String container, String bandwidthLimitKbps, int packetLossPercentage, int delayMs) {
        for (String iface : getInterfaces(container)) {
            String deleteCommand = String.format("docker exec %s tc qdisc del dev %s root", container, iface);
            String addCommand = String.format(
                    "docker exec %s tc qdisc add dev %s root netem delay %dms loss %d%% rate %sbit",
                    container, iface, delayMs, packetLossPercentage, bandwidthLimitKbps);
            executeCommand(deleteCommand);
            executeCommand(addCommand);
        }
    }

    @Override
    public void clearLimitations(String container) {
        List<String> interfaces = getInterfaces(container);
        for (String iface : interfaces) {
            String command = String.format("docker exec %s tc qdisc del dev %s root", container, iface);
            executeCommand(command);
        }
    }

    public String readProperties(String container) {
        List<String> interfaces = getInterfaces(container);
        String command = String.format("docker exec %s tc qdisc show dev %s", container, interfaces.get(0));
        return executeCommand(command);
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

    @Override
    public ResponseEntity<String> updateContainerResources(String containerName,
                                                           Long memory,
                                                           Long memorySwap,
                                                           Integer cpuQuota,
                                                           Integer cpuPeriod,
                                                           Integer cpuShares) {
        try {
            UpdateContainerCmd updateCmd = dockerClient.updateContainerCmd(containerName);

            if (memory != null) updateCmd.withMemory(memory);
            if (memorySwap != null) updateCmd.withMemorySwap(memorySwap);
            if (cpuQuota != null) updateCmd.withCpuQuota(cpuQuota);
            if (cpuPeriod != null) updateCmd.withCpuPeriod(cpuPeriod);
            if (cpuShares != null) updateCmd.withCpuShares(cpuShares);

            updateCmd.exec();
            return ResponseEntity.ok("资源限制已成功更新");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("更新失败：" + e.getMessage());
        }
    }


    private String executeCommand(String command) {
        ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
        try {
            Process process = processBuilder.start();

            // 打印输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            String output = "";
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                output = line;
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("命令执行成功: " + command);
                return output;
            } else {
                System.out.println("命令执行失败: " + command);
                return output;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }
}
