package org.example.dockernetwork.Entity;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ClientThread extends Thread{

    private final String container;
    private final String serverIp;
    private final int port;
    private final int time;
    private final int branch;

    public ClientThread(String container, String serverIp, int port, int time, int branch){
        this.container = container;
        this.serverIp = serverIp;
        this.port = port;
        this.time = time;
        this.branch = branch;
    }

    @Override
    public void run() {
        // 使用传递的参数
        System.out.println("线程 " + Thread.currentThread().getId() + " 正在向端口 " + port + " 传输数据");
        try{
            String command = "docker exec " + container + " iperf3 -c " + serverIp + " -p " + port + " -t " + time + " -P " + branch;
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("bash", "-c", command);
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);  // 输出结果
            }
            // 等待进程结束
            int exitCode = process.waitFor();
            System.out.println("iperf3 执行完毕，退出代码: " + exitCode);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
