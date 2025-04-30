package org.example.dockernetwork.Entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StaticRouteEntity {
    private String targetIp;
    private String nextHopIp;

    @JsonCreator
    public StaticRouteEntity(@JsonProperty("targetIp") String targetIp, @JsonProperty("nextHopIp") String nextHopIp) {
        this.targetIp = targetIp;
        this.nextHopIp = nextHopIp;
    }

}
