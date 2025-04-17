package org.example.dockernetwork.Service.Impl;

import org.example.dockernetwork.Service.FirewallService;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class FirewallServiceImpl implements FirewallService {

    @Override
    public String set(String container1, String container2Ip, String op, String direction) {
        String command;

        if ("INPUT".equalsIgnoreCase(direction)) {
            command = String.format("docker exec %s iptables -%s INPUT -s %s -j DROP", container1, op, container2Ip);
        } else if ("OUTPUT".equalsIgnoreCase(direction)) {
            command = String.format("docker exec %s iptables -%s OUTPUT -d %s -j DROP", container1, op, container2Ip);
        } else {
            return "错误：direction 参数只能为 INPUT 或 OUTPUT";
        }

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
            Process process = processBuilder.start();

            StringBuilder output = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                return "防火墙规则成功设置。\n" + output;
            } else {
                return "命令执行失败，退出码: " + exitCode + "\n" + output;
            }

        } catch (IOException | InterruptedException e) {
            return "执行异常: " + e.getMessage();
        }
    }
}
