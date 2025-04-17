package org.example.dockernetwork.Service;

import org.example.dockernetwork.Entity.DockerContainer;
import org.example.dockernetwork.Entity.DockerNetwork;

import java.util.List;

public interface DockerComposeService {

    public void create(List<DockerNetwork> dockerNetworkList,
                       List<DockerContainer> dockerContainerList,
                       String file);

}
