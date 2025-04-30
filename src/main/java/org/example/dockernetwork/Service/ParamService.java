package org.example.dockernetwork.Service;

import org.springframework.http.ResponseEntity;

public interface ParamService {

    void setDelay(String container, int delayInMs);

    void setBandwidth(String container, String bandwidthLimitInKbps, String burst, int latency);

    void setPacketLoss(String container, int lossPercentage);

    void setProperties(String container, String bandwidthLimitKbps, int packetLossPercentage, int delayMs);

    ResponseEntity<String> updateContainerResources(String containerName,
                                                           Long memory,
                                                           Long memorySwap,
                                                           Integer cpuQuota,
                                                           Integer cpuPeriod,
                                                           Integer cpuShares);

    void clearLimitations(String container);

    String readProperties(String container);
}
