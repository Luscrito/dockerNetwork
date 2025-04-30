package org.example.dockernetwork.Service.Impl;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.StatisticNetworksConfig;
import com.github.dockerjava.api.model.Statistics;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import org.example.dockernetwork.Entity.ContainerBandwidth;
import org.example.dockernetwork.Entity.ContainerNetworkStats;
import org.example.dockernetwork.Service.DockerStatsService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class DockerStatsServiceImpl implements DockerStatsService {

    private final DockerClient dockerClient;

    public DockerStatsServiceImpl() {
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
    public CompletableFuture<ContainerNetworkStats> getNetworkStats(String containerId) {
        CompletableFuture<ContainerNetworkStats> future = new CompletableFuture<>();

        dockerClient.statsCmd(containerId).exec(new ResultCallback.Adapter<Statistics>() {
            @Override
            public void onNext(Statistics stats) {
                long rx = 0, tx = 0;
                Map<String, StatisticNetworksConfig> networks = stats.getNetworks();
                if (networks != null) {
                    for (StatisticNetworksConfig config : networks.values()) {
                        rx += config.getRxBytes();
                        tx += config.getTxBytes();
                    }
                }

                future.complete(new ContainerNetworkStats(rx, tx));

                // 立即取消并关闭连接
                this.onComplete();
            }

            @Override
            public void onError(Throwable throwable) {
                future.completeExceptionally(throwable);
            }
        });
        return future;
    }

    @Override
    public CompletableFuture<ContainerBandwidth> calculateBandwidth(String containerName, int intervalMillis) {
        CompletableFuture<ContainerBandwidth> future = new CompletableFuture<>();

        // 查找容器 ID
        InspectContainerResponse containerInfo = dockerClient.inspectContainerCmd(containerName).exec();
        String containerId = containerInfo.getId();

        if (containerId == null) {
            future.completeExceptionally(new IllegalArgumentException("Container '" + containerName + "' not found"));
            return future;
        }

        // 第一次采样
        getNetworkStats(containerId).thenCompose(first -> {
            // 延迟 intervalMillis 后再采样
            return CompletableFuture.supplyAsync(() -> {
                try {
                    Thread.sleep(intervalMillis);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }).thenCompose(v -> getNetworkStats(containerId).thenApply(second -> {
                long rxPerSec = (second.getRxBytes() - first.getRxBytes()) * 1000 / intervalMillis;
                long txPerSec = (second.getTxBytes() - first.getTxBytes()) * 1000 / intervalMillis;
                return new ContainerBandwidth(rxPerSec, txPerSec);
            }));
        }).whenComplete((result, ex) -> {
            if (ex != null) {
                future.completeExceptionally(ex);
            } else {
                future.complete(result);
            }
        });
        return future;
    }

    @Override
    public CompletableFuture<ContainerBandwidth> calculateInternalBandwidth(String containerName, int intervalMillis) {
        CompletableFuture<ContainerBandwidth> future = new CompletableFuture<>();

        // 查找容器 ID
        InspectContainerResponse containerInfo = dockerClient.inspectContainerCmd(containerName).exec();
        String containerId = containerInfo.getId();

        if (containerId == null) {
            future.completeExceptionally(new IllegalArgumentException("Container '" + containerName + "' not found"));
            return future;
        }

        try {
            // 第一次采样
            long[] first = readInternalNetBytes(containerId);

            // 延迟一段时间后再次采样
            CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(intervalMillis);
                    long[] second = readInternalNetBytes(containerId);

                    long rxRate = (second[0] - first[0]) * 1000 / intervalMillis;
                    long txRate = (second[1] - first[1]) * 1000 / intervalMillis;

                    future.complete(new ContainerBandwidth(rxRate, txRate));
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            });
        } catch (Exception e) {
            future.completeExceptionally(e);
        }

        return future;
    }

    private long[] readInternalNetBytes(String containerId) throws InterruptedException {
        String command = "cat /proc/net/dev";
        String output = dockerClient.execCreateCmd(containerId)
                .withCmd("sh", "-c", command)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .exec()
                .getId();

        StringBuilder sb = new StringBuilder();

        dockerClient.execStartCmd(output).exec(new ResultCallback.Adapter<Frame>() {
            @Override
            public void onNext(Frame frame) {
                sb.append(new String(frame.getPayload()));
            }
        }).awaitCompletion();

        return parseNetDevOutput(sb.toString(), "eth0");
    }

    private long[] parseNetDevOutput(String output, String iface) {
        String[] lines = output.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith(iface + ":")) {
                String[] parts = line.split("[:\\s]+");
                long rxBytes = Long.parseLong(parts[1]);
                long txBytes = Long.parseLong(parts[9]);
                return new long[]{rxBytes, txBytes};
            }
        }
        return new long[]{0, 0};
    }

}

