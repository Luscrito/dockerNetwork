package org.example.dockernetwork.Service.Impl;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ContainerNetwork;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.example.dockernetwork.Service.TopologyService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TopologyServiceImpl implements TopologyService {

    private final DockerClient dockerClient;

    public TopologyServiceImpl() {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("unix:///var/run/docker.sock")
                .build();

        this.dockerClient = DockerClientBuilder.getInstance(config)
                .withDockerHttpClient(new ApacheDockerHttpClient.Builder()
                        .dockerHost(config.getDockerHost())
                        .build())
                .build();
    }

    public Map<String, Object> getTopology() {
        List<Node> nodes = new ArrayList<>();
        List<Link> links = new ArrayList<>();
        Map<String, List<String>> networkToContainers = new HashMap<>();

        List<Container> containers = dockerClient.listContainersCmd()
                .withShowAll(true)
                .exec();

        // 先收集所有容器节点
        for (Container container : containers) {
            String containerId = container.getId();
            String containerName = container.getNames()[0].replaceFirst("/", "");

            if(containerName.equals("portainer")) continue;

            nodes.add(new Node(containerId, containerName, "container", 50));

            Map<String, ContainerNetwork> networks = container.getNetworkSettings().getNetworks();
            if (networks != null) {
                for (String networkName : networks.keySet()) {
                    networkToContainers.computeIfAbsent(networkName, k -> new ArrayList<>()).add(containerId);
                }
            }
        }

        // 再处理所有网络，看哪些容器之间有连接
        for (Map.Entry<String, List<String>> entry : networkToContainers.entrySet()) {
            List<String> connectedContainers = entry.getValue();
            if (connectedContainers.size() == 2) {
                // 只处理恰好两个容器的网络
                String sourceId = connectedContainers.get(0);
                String targetId = connectedContainers.get(1);
                links.add(new Link(sourceId, targetId));
            }
            // 如果有更多容器，也可以扩展，比如构建一个全连接，但你目前说最多两个，这样就够
        }

        Map<String, Object> result = new HashMap<>();
        result.put("nodes", nodes);
        result.put("links", links);
        return result;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    private static class Node {
        private String id;
        private String name;
        private String type;       // container
        private int symbolSize;    // 节点大小
    }

    @Getter
    @Setter
    @AllArgsConstructor
    private static class Link {
        private String source;     // 容器 ID
        private String target;     // 容器 ID
    }
}
