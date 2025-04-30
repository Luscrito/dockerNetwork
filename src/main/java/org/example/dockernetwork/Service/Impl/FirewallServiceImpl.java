package org.example.dockernetwork.Service.Impl;

import org.example.dockernetwork.Service.FirewallService;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class FirewallServiceImpl implements FirewallService {

    @Override
    public String addRule(String containerName, String chain, String rule) throws IOException, InterruptedException {
        return executeCommand(containerName, "-A", chain, rule);
    }

    @Override
    public String deleteRule(String containerName, String chain, String rule) throws IOException, InterruptedException {
        return executeCommand(containerName, "-D", chain, rule);
    }

    @Override
    public String deleteRuleByIndex(String containerName, String chain, int index) throws IOException, InterruptedException {
        String[] command = {"docker", "exec", containerName, "iptables", "-D", chain, String.valueOf(index)};
        return runCommand(command);
    }

    @Override
    public String clearAllRules(String containerName) throws IOException, InterruptedException {
        String[] command = {"docker", "exec", containerName, "iptables", "-F"};
        return runCommand(command);
    }

    @Override
    public String listRules(String containerName, String chain) throws IOException, InterruptedException {
        String[] command = {"docker", "exec", containerName, "iptables", "-L", chain, "-n", "-v"};
        return runCommand(command);
    }

    private String executeCommand(String containerName, String operation, String chain, String rule) throws IOException, InterruptedException {
        // 拆分 rule 字符串为多个参数
        String[] ruleParts = rule.trim().split("\\s+");

        // 构造完整命令
        String[] baseCommand = new String[] { "docker", "exec", containerName, "iptables", operation, chain };
        String[] fullCommand = new String[baseCommand.length + ruleParts.length];
        System.arraycopy(baseCommand, 0, fullCommand, 0, baseCommand.length);
        System.arraycopy(ruleParts, 0, fullCommand, baseCommand.length, ruleParts.length);

        // 调用原始执行逻辑
        return runCommand(fullCommand);
    }

    private String runCommand(String[] command) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(command);
        Process process = builder.start();

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                StringBuilder errorOutput = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorOutput.append(line).append("\n");
                }
                throw new IOException("命令执行失败：" + errorOutput.toString());
            }
        }

        try (BufferedReader outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = outputReader.readLine()) != null) {
                result.append(line).append("\n");
            }
            return result.toString().isEmpty() ? "操作成功。" : result.toString();
        }
    }
}
