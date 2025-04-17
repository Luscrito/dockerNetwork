package org.example.dockernetwork.Service.Impl;

import org.example.dockernetwork.Service.ParamService;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class ParamServiceImpl implements ParamService {

    @Override
    public void setDelay(String container, int delayInMs) {
        for (String iface : getInterfaces(container)) {
            String command = String.format("docker exec %s tc qdisc add dev %s root netem delay %dms", container, iface, delayInMs);
            executeCommand(command);
        }
    }

    @Override
    public void setPacketLoss(String container, int lossPercentage) {
        for (String iface : getInterfaces(container)) {
            String command = String.format("docker exec %s tc qdisc add dev %s root netem loss %d%%", container, iface, lossPercentage);
            executeCommand(command);
        }
    }

    @Override
    public void setBandwidth(String container, String bandwidthLimit, String burst, int latency) {
        for (String iface : getInterfaces(container)) {
            String command = String.format(
                    "docker exec %s tc qdisc add dev %s root tbf rate %sbit burst %sbit latency %dms",
                    container, iface, bandwidthLimit, burst, latency);
            executeCommand(command);
        }
    }

    @Override
    public void setProperties(String container, String bandwidthLimitKbps, int packetLossPercentage, int delayMs) {
        for (String iface : getInterfaces(container)) {
            String command = String.format(
                    "docker exec %s tc qdisc change dev %s root netem delay %dms loss %d%% rate %sbit",
                    container, iface, delayMs, packetLossPercentage, bandwidthLimitKbps);
            executeCommand(command);
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


    public List<String> getInterfaces(String container) {
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

    private void executeCommand(String command) {
        ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
        try {
            Process process = processBuilder.start();

            // 打印输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("命令执行成功: " + command);
            } else {
                System.out.println("命令执行失败: " + command);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
