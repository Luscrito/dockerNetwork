package org.example.dockernetwork.Service.Impl;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ContainerNetwork;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Network;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import org.example.dockernetwork.Entity.DockerContainer;
import org.example.dockernetwork.Service.ContainerService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ContainerServiceImpl implements ContainerService {

    private final DockerClient dockerClient;

    public ContainerServiceImpl() {
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
    public List<Map<String, String>> list() {
        List<Map<String, String>> containerInfo = new ArrayList<>();

        try {
            List<Container> containers = dockerClient.listContainersCmd().withShowAll(true).exec();
            for (Container container : containers) {
                Map<String, String> info = new HashMap<>();
                info.put("id", container.getId());
                info.put("name", container.getNames()[0]);
                info.put("image", container.getImage());
                info.put("status", container.getStatus());
                containerInfo.add(info);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return containerInfo;
    }

    @Override
    public void create(DockerContainer container) {
        try {
            CreateContainerResponse response = dockerClient.createContainerCmd(container.getImage())
                    .withName(container.getName())
                    .withHostConfig(HostConfig.newHostConfig())
                    .exec();
            String createId = response.getId();

            //断开bridge网络
            dockerClient.disconnectFromNetworkCmd()
                    .withContainerId(createId)
                    .withNetworkId("bridge") // `bridge` 是默认的网络名称
                    .withForce(true) // 强制断开
                    .exec();

            dockerClient.startContainerCmd(createId).exec();
            System.out.println("容器名称: " + container.getName() + " (ID: " + createId + ")");

            for (int i = 0; i < container.getNetworks().size(); i++) {
                String network = container.getNetworks().get(i);
                dockerClient.connectToNetworkCmd()
                        .withContainerId(createId)
                        .withNetworkId(network)
                        .exec();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void remove(String containerName) {
        try {
            // 遍历容器找到指定名称的容器ID
            List<Container> containers = dockerClient.listContainersCmd().withShowAll(true).exec();
            String deleteId = null;

            for (Container container : containers) {
                for (String name : container.getNames()) {
                    // Docker 容器名称前面带有 "/" 前缀
                    if (name.equals("/" + containerName)) {
                        deleteId = container.getId();
                        break;
                    }
                }
            }

            if (deleteId != null) {
                dockerClient.removeContainerCmd(deleteId).withForce(true).exec();
                System.out.println("删除容器名称: " + containerName + " (ID: " + deleteId + ")");
            }
            else {
                System.out.println("不存在网络名称为: " + containerName);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connect(String containerName, List<String> networks) {
        try {
            // 查找容器ID
            List<Container> containers = dockerClient.listContainersCmd().withShowAll(true).exec();
            String containerId = null;

            for (Container container : containers) {
                for (String name : container.getNames()) {
                    if (name.equals("/" + containerName)) {
                        containerId = container.getId();
                        break;
                    }
                }
            }

            if (containerId == null) {
                return ;
            }

            for (String networkName : networks) {
                // 获取网络ID
                List<Network> dockerNetworks = dockerClient.listNetworksCmd().exec();
                String networkId = null;
                for (Network net : dockerNetworks) {
                    if (net.getName().equals(networkName)) {
                        networkId = net.getId();
                        break;
                    }
                }

                if (networkId != null) {
                    dockerClient.connectToNetworkCmd()
                            .withContainerId(containerId)
                            .withNetworkId(networkId)
                            .exec();
                    System.out.println("已将容器 " + containerName + " 连接到网络: " + networkName);
                }
                else {
                    System.out.println("未找到网络: " + networkName);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void disconnect(String containerName, List<String> networks) {
        try {
            // 查找容器 ID
            List<Container> containers = dockerClient.listContainersCmd().withShowAll(true).exec();
            String containerId = null;

            for (Container container : containers) {
                for (String name : container.getNames()) {
                    if (name.equals("/" + containerName)) {
                        containerId = container.getId();
                        break;
                    }
                }
            }

            if (containerId == null) {
                System.out.println("未找到容器: " + containerName);
                return;
            }

            // 遍历断开连接
            for (String networkName : networks) {
                // 查找对应的网络 ID
                List<Network> allNetworks = dockerClient.listNetworksCmd().exec();
                String networkId = null;

                for (Network network : allNetworks) {
                    if (network.getName().equals(networkName)) {
                        networkId = network.getId();
                        break;
                    }
                }

                if (networkId != null) {
                    dockerClient.disconnectFromNetworkCmd()
                            .withContainerId(containerId)
                            .withNetworkId(networkId)
                            .withForce(true)
                            .exec();
                    System.out.println("已将容器 " + containerName + " 从网络 " + networkName + " 中移除");
                }
                else {
                    System.out.println("未找到网络: " + networkName);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String start(String containerName) {
        try {
            List<Container> containers = dockerClient.listContainersCmd()
                    .withShowAll(true)
                    .exec();

            for (Container container : containers) {
                String[] names = container.getNames();
                if (names != null) {
                    for (String name : names) {
                        if (name.equals("/" + containerName)) {
                            dockerClient.startContainerCmd(container.getId()).exec();
                            return "已启动容器："+containerName;
                        }
                    }
                }
            }
            return "未找到容器: " + containerName;
        } catch (Exception e) {
            e.printStackTrace();
            return "执行出错";
        }
    }

    @Override
    public String stop(String containerName) {
        try {
            List<Container> containers = dockerClient.listContainersCmd()
                    .withShowAll(true)
                    .exec();

            for (Container container : containers) {
                String[] names = container.getNames();
                if (names != null) {
                    for (String name : names) {
                        if (name.equals("/" + containerName)) {
                            dockerClient.stopContainerCmd(container.getId()).exec();
                            return "已停止容器："+containerName;
                        }
                    }
                }
            }
            return "未找到容器: " + containerName;
        } catch (Exception e) {
            e.printStackTrace();
            return "执行出错";
        }
    }

    @Override
    public Map<String, String> getIp(String containerName) {
        Map<String, String> ipMap = new HashMap<>();
        try {
            InspectContainerResponse containerInfo = dockerClient.inspectContainerCmd(containerName).exec();

            Map<String, ContainerNetwork> networks = containerInfo.getNetworkSettings().getNetworks();
            for (Map.Entry<String, ContainerNetwork> entry : networks.entrySet()) {
                String networkName = entry.getKey();
                String ipAddress = entry.getValue().getIpAddress();
                ipMap.put(networkName, ipAddress);
            }

            return ipMap;
        } catch (Exception e) {
            e.printStackTrace();
            ipMap.put("error", "查询失败: " + e.getMessage());
            return ipMap;
        }
    }
}
