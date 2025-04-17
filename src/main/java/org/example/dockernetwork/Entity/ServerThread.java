package org.example.dockernetwork.Entity;

public class ServerThread extends Thread{

    private final String container;
    private final String serverIp;
    private final int port;

    public ServerThread(String container, String serverIp, int port){
        this.container = container;
        this.serverIp = serverIp;
        this.port = port;
    }

    @Override
    public void run() {
        // 使用传递的参数
        System.out.println("线程 " + Thread.currentThread().getId() + " 正在开放端口 " + port);
        try {
            String command = "docker exec -d " + container + " iperf3 -s -B " + serverIp + " -p " + port;
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("bash", "-c", command);
            Process process = processBuilder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
