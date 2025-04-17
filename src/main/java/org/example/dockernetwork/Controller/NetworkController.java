package org.example.dockernetwork.Controller;

import lombok.extern.slf4j.Slf4j;
import org.example.dockernetwork.Entity.DockerNetwork;
import org.example.dockernetwork.Service.ContainerService;
import org.example.dockernetwork.Service.NetworkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@Slf4j
@RequestMapping("/network")
public class NetworkController {

    @Autowired
    private NetworkService networkService;
    @Autowired
    private ContainerService containerService;

    @GetMapping("/list")
    public Map<String, List<String>> listNetwork() {
        return networkService.list();
    }

    @PostMapping("/create")
    public void createNetwork(@RequestBody DockerNetwork network) {
        networkService.create(network);
    }

    @DeleteMapping("/delete/{networkName}")
    public void removeNetwork(@PathVariable String networkName) {
        networkService.remove(networkName);
    }

    @PutMapping("/connect")
    public void connectContainerToNetworks(@RequestParam String containerName, @RequestBody List<String> networks) {
        containerService.connect(containerName, networks);
    }

    @PutMapping("/disconnect")
    public void disconnectContainerFromNetworks(@RequestParam String containerName, @RequestBody List<String> networks) {
        containerService.disconnect(containerName, networks);
    }
}