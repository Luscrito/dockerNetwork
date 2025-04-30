package org.example.dockernetwork.Service;

import org.example.dockernetwork.Entity.ContainerBandwidth;
import org.example.dockernetwork.Entity.ContainerNetworkStats;

import java.util.concurrent.CompletableFuture;

public interface DockerStatsService {

    CompletableFuture<ContainerNetworkStats> getNetworkStats(String container);

    CompletableFuture<ContainerBandwidth> calculateBandwidth(String containerName, int intervalMillis);

    CompletableFuture<ContainerBandwidth> calculateInternalBandwidth(String containerName, int intervalMillis);
}
