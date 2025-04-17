package org.example.dockernetwork.Service.Impl;

import org.example.dockernetwork.Service.TestService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TestServiceImpl implements TestService {

    @Override
    public String HttpTest(String container1, String container2Ip) {
        // 默认 HTTP 端口为 80，如需修改可自行添加端口号
        String url = "http://" + container2Ip;
        String command = String.format("docker exec %s curl -s -o /dev/null -w \"%%{http_code}\" %s", container1, url);

        ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);

        try {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String statusCode = reader.readLine(); // 获取 HTTP 状态码

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                return "容器 " + container1 + " 成功访问 " + container2Ip + "，HTTP 状态码：" + statusCode;
            } else {
                return "容器 " + container1 + " 无法访问 " + container2Ip + "，命令退出码：" + exitCode;
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "执行失败";
        }
    }

    @Override
    public String TCPTest(String container1, String container2, String container2Ip, int size, int time) {
        StringBuilder result = new StringBuilder();
        try {
            String serverCmd = "docker exec " + container2 + " iperf3 -s";
            String clientCmd = "docker exec " + container1 + " iperf3 -c " + container2Ip + " -t " + time + " -w " + size;

            ProcessBuilder serverPB = new ProcessBuilder("bash", "-c", serverCmd);
            ProcessBuilder clientPB = new ProcessBuilder("bash", "-c", clientCmd);

            Process serverProcess = serverPB.start();
            Thread.sleep(1000); // 等待 server 启动
            Process clientProcess = clientPB.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(clientProcess.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }

            int exitCode = clientProcess.waitFor();
            result.append("TCP 测试完成，退出代码：").append(exitCode);

            serverProcess.destroy();
        } catch (Exception e) {
            result.append("TCP 测试异常：").append(e.getMessage());
        }
        return result.toString();
    }

    @Override
    public String UDPTest(String container1, String container2, String container2Ip, String velocity, int size) {
        StringBuilder result = new StringBuilder();
        try {
            String serverCmd = "docker exec " + container2 + " iperf3 -s";
            String clientCmd = "docker exec " + container1 + " iperf3 -c " + container2Ip + " -u -b " + velocity + " -l " + size;

            ProcessBuilder serverPB = new ProcessBuilder("bash", "-c", serverCmd);
            ProcessBuilder clientPB = new ProcessBuilder("bash", "-c", clientCmd);

            Process serverProcess = serverPB.start();
            Thread.sleep(1000); // 等待 server 启动
            Process clientProcess = clientPB.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(clientProcess.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }

            int exitCode = clientProcess.waitFor();
            result.append("UDP 测试完成，退出代码：").append(exitCode);

            serverProcess.destroy();
        } catch (Exception e) {
            result.append("UDP 测试异常：").append(e.getMessage());
        }
        return result.toString();
    }

    @Override
    public String NetcatTest(String container1, String container2Ip) {
        StringBuilder result = new StringBuilder();
        try {
            String cmd = "docker exec " + container1 + " nc -zv " + container2Ip + " 80";
            ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", cmd);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            result.append("Netcat 测试完成，状态：").append(exitCode == 0 ? "成功" : "失败");

        } catch (Exception e) {
            result.append("Netcat 测试异常：").append(e.getMessage());
        }
        return result.toString();
    }

    @Override
    public String PingTest(String container1, String container2Ip, int size) {
        StringBuilder result = new StringBuilder();
        try {
            String cmd = "docker exec " + container1 + " ping -c 50 -s " + size + " " + container2Ip;
            ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", cmd);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            result.append("Ping 测试完成，状态：").append(exitCode == 0 ? "成功" : "失败");

        } catch (Exception e) {
            result.append("Ping 测试异常：").append(e.getMessage());
        }
        return result.toString();
    }
}
