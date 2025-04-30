package org.example.dockernetwork.Service;

import org.example.dockernetwork.Entity.DockerContainer;

import java.util.List;
import java.util.Map;

public interface ContainerService {

    List<Map<String, String>> list();

    void create(DockerContainer container);

    void remove(String containerName);

    void connect(String containerName, List<String> networks);

    void disconnect(String containerName, List<String> networks);

    String start(String containerName);

    String stop(String containerName);

    Map<String,String> getIp(String containerName);
}
