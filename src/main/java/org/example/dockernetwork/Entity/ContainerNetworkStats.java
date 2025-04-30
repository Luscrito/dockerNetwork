package org.example.dockernetwork.Entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContainerNetworkStats {
    private long rxBytes;
    private long txBytes;

    @JsonCreator
    public ContainerNetworkStats(@JsonProperty("rxBytes") long rxBytes, @JsonProperty("txBytes") long txBytes) {
        this.rxBytes = rxBytes;
        this.txBytes = txBytes;
    }
}
