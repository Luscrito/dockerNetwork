package org.example.dockernetwork.Entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContainerBandwidth {
    private long rxBytesPerSec;
    private long txBytesPerSec;

    @JsonCreator
    public ContainerBandwidth(@JsonProperty("rxBytesPerSec") long rxBytesPerSec, @JsonProperty("txBytesPerSec") long txBytesPerSec) {
        this.rxBytesPerSec = rxBytesPerSec;
        this.txBytesPerSec = txBytesPerSec;
    }
}
