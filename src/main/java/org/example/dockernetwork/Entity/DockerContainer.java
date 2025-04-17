package org.example.dockernetwork.Entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class DockerContainer {
    private String image;
    private String name;
    private List<String> networks = new ArrayList<>();

    @JsonCreator
    public DockerContainer(@JsonProperty("image") String image, @JsonProperty("name") String name, @JsonProperty("networks") List<String> networks) {
        this.image = image;
        this.name = name;
        this.networks = networks;
    }

    public DockerContainer(String image, String name) {
        this.image = image;
        this.name = name;
    }

    public void addNetwork(String network){
        this.networks.add(network);
    }
}
