package org.example.dockernetwork.Service;

import org.example.dockernetwork.Entity.DockerContainer;

import java.util.List;
import java.util.Map;

public interface ContainerService {

    public List<Map<String, String>> list();

    public void create(DockerContainer container);

    public void remove(String containerName);

    public void connect(String containerName, List<String> networks);

    public void disconnect(String containerName, List<String> networks);

    public String start(String containerName);

    public String stop(String containerName);

    public Map<String,String> getIp(String containerName);
}
