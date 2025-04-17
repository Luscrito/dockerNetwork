package org.example.dockernetwork.Service;

public interface ParamService {

    public void setDelay(String container, int delayInMs);

    public void setBandwidth(String container, String bandwidthLimitInKbps, String burst, int latency);

    public void setPacketLoss(String container, int lossPercentage);

    public void setProperties(String container, String bandwidthLimitKbps, int packetLossPercentage, int delayMs);

    public void clearLimitations(String container);
}
