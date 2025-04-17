package org.example.dockernetwork.Service.Impl;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateNetworkResponse;
import com.github.dockerjava.api.model.Network;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import org.example.dockernetwork.Entity.DockerNetwork;
import org.example.dockernetwork.Service.NetworkService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class NetworkServiceImpl implements NetworkService {

    private final DockerClient dockerClient;

    public NetworkServiceImpl() {
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
    public Map<String, List<String>> list() {

        Map<String, List<String>> NetInfo = new HashMap<>();

        try {
            // 获取所有Docker网络
            List<Network> networks = dockerClient.listNetworksCmd().exec();
            for (Network network : networks) {
                List<String> NetContainers = new ArrayList<>();
                String networkId = network.getId();
                String networkName = network.getName();
                System.out.println("网络名称: " + networkName + " (ID: " + networkId + ")");

                // 获取网络详细信息（包含关联的容器）
                Map<String, Network.ContainerNetworkConfig> containers = dockerClient.inspectNetworkCmd()
                        .withNetworkId(networkId)
                        .exec()
                        .getContainers();

                // 提取容器信息
                if (containers != null && !containers.isEmpty()) {
                    for (Map.Entry<String, Network.ContainerNetworkConfig> entry : containers.entrySet()) {
                        String containerId = entry.getKey();
                        Network.ContainerNetworkConfig containerSettings = entry.getValue();

                        // 获取容器名称
                        String containerName = containerSettings.getName();
                        System.out.println("  容器名称: " + containerName + " (ID: " + containerId + ")");
                        NetContainers.add(containerSettings.getName());
                    }
                }
                else {
                    System.out.println("没有容器连接到这个网络。");
                }
                NetInfo.put(network.getName(),NetContainers);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return NetInfo;
    }

    @Override
    public void create(DockerNetwork network) {

        Network.Ipam ipam = null;

        if (network.getSubnet() != null && !network.getSubnet().isEmpty()) {
            Network.Ipam.Config ipamConfig = new Network.Ipam.Config();
            ipamConfig.withSubnet(network.getSubnet());
            ipamConfig.withGateway(network.getGateway());
            ipam = new Network.Ipam().withConfig(Collections.singletonList(ipamConfig));
        }

        CreateNetworkResponse response = dockerClient.createNetworkCmd()
                .withName(network.getName())
                .withDriver(network.getDriver())
                .withIpam(ipam)
                .exec();
        String createId = response.getId();
        System.out.println("网络名称: " + network.getName() + " (ID: " + createId + ")");
    }

    @Override
    public void create(String network) {

        Network.Ipam ipam = null;
        Network.Ipam.Config ipamConfig = new Network.Ipam.Config();
        ipam = new Network.Ipam().withConfig(Collections.singletonList(ipamConfig));

        CreateNetworkResponse response = dockerClient.createNetworkCmd()
                .withName(network)
                .withDriver("bridge")
                .exec();
        String createId = response.getId();
        System.out.println("网络名称: " + network + " (ID: " + createId + ")");
    }

    @Override
    public void remove(String networkName) {

        try {
            // 获取所有网络，找到匹配的名字对应的ID
            List<Network> networks = dockerClient.listNetworksCmd().exec();
            String deleteId = null;

            for (Network network : networks) {
                if (network.getName().equals(networkName)) {
                    deleteId = network.getId();
                    break;
                }
            }

            if (deleteId != null) {
                dockerClient.removeNetworkCmd(deleteId).exec();
                System.out.println("删除网络名称: " + networkName + " (ID: " + deleteId + ")");
            } else {
                System.out.println("不存在网络名称为: " + networkName);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
