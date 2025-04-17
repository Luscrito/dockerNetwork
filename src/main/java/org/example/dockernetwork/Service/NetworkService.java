package org.example.dockernetwork.Service;

import org.example.dockernetwork.Entity.DockerNetwork;

import java.util.List;
import java.util.Map;

public interface NetworkService {

    public Map<String, List<String>> list();

    public void create(DockerNetwork network);

    public void create(String network);

    public void remove(String networkName);
}
