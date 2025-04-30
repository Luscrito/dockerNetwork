package org.example.dockernetwork.Service;

import org.example.dockernetwork.Entity.DockerNetwork;

import java.util.List;
import java.util.Map;

public interface NetworkService {

    Map<String, List<String>> list();

    void create(DockerNetwork network);

    void create(String network);

    void remove(String networkName);

    void disconnectAllContainers(String networkName);
}
