package org.example.dockernetwork.Controller;


import lombok.extern.slf4j.Slf4j;
import org.example.dockernetwork.Entity.ContainerBandwidth;
import org.example.dockernetwork.Entity.ContainerNetworkStats;
import org.example.dockernetwork.Service.DockerStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/stats")
@Slf4j
public class DockerStatsController {

    @Autowired
    private DockerStatsService dockerStatsService;

    @GetMapping("/networkStats/{containerName}")
    public CompletableFuture<ContainerNetworkStats> getNetworkStats(@PathVariable String containerName) {
        return dockerStatsService.getNetworkStats(containerName);
    }

    @GetMapping("/bandwidth/{containerName}")
    public CompletableFuture<ContainerBandwidth> getBandwidth(@PathVariable String containerName,
                                                              @RequestParam(defaultValue = "1000") int interval) {
        return dockerStatsService.calculateBandwidth(containerName, interval);
    }

    @GetMapping("/containerBandwidth/{containerName}")
    public CompletableFuture<ContainerBandwidth> getContainerBandwidth(
            @PathVariable String containerName,
            @RequestParam(defaultValue = "1000") int intervalMillis) {

        return dockerStatsService.calculateInternalBandwidth(containerName, intervalMillis);
    }

}
