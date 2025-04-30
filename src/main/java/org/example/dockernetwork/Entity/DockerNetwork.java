package org.example.dockernetwork.Entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DockerNetwork {
    private String driver;
    private String name;
    private String subnet;
    private String gateway = null;

    @JsonCreator
    public DockerNetwork(@JsonProperty("driver") String driver, @JsonProperty("subnet") String subnet, @JsonProperty("name") String name, @JsonProperty("gateway") String gateway) {
        this.driver = driver;
        this.subnet = subnet;
        this.name = name;
        this.gateway = gateway;
    }

    public DockerNetwork( String driver,  String subnet,  String name) {
        this.driver = driver;
        this.subnet = subnet;
        this.name = name;
    }

}
