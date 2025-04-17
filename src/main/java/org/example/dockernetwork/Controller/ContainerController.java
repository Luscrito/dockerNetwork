package org.example.dockernetwork.Controller;

import lombok.extern.slf4j.Slf4j;
import org.example.dockernetwork.Entity.DockerContainer;
import org.example.dockernetwork.Service.ContainerService;
import org.example.dockernetwork.Service.NetworkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@Slf4j
@RequestMapping("/container")
public class ContainerController {

    @Autowired
    private ContainerService containerService;

    @Autowired
    private NetworkService networkService;


    @GetMapping("/list")
    public List<Map<String, String>> listContainers() {
        return containerService.list();
    }

    @PostMapping("/create")
    public void createContainer(@RequestBody DockerContainer container) {
        containerService.create(container);
    }

    @DeleteMapping("/delete/{containerName}")
    public void removeContainerByName(@PathVariable String containerName) {
        containerService.remove(containerName);
    }

    @PostMapping("/start/{containerName}")
    public String startContainer(@PathVariable String containerName) {
        return containerService.start(containerName);
    }

    @PostMapping("/stop/{containerName}")
    public String stopContainer(@PathVariable String containerName) {
        return containerService.stop(containerName);
    }

    @GetMapping("/ip/{containerName}")
    public Map<String,String> getContainerIp(@PathVariable String containerName) {
        return containerService.getIp(containerName);
    }

    @PostMapping("/connect")
    public void connectContainers(@RequestParam String container1, @RequestParam String container2) {
        String networkName;
        if(container1.compareTo(container2) < 0) {
            networkName = container1+"To"+container2;
        }
        else
            networkName = container2+"To"+container1;
        System.out.println(networkName);
        networkService.create(networkName);
        List<String> networks = new ArrayList<>();
        networks.add(networkName);
        containerService.connect(container1, networks);
        containerService.connect(container2, networks);
    }

    @PostMapping("/disconnect")
    public void disconnectContainers(@RequestParam String container1, @RequestParam String container2) {
        String networkName;
        if(container1.compareTo(container2) < 0) {
            networkName = container1+"To"+container2;
        }
        else
            networkName = container2+"To"+container1;
        System.out.println(networkName);
        List<String> networks = new ArrayList<>();
        networks.add(networkName);
        containerService.disconnect(container1, networks);
        containerService.disconnect(container2, networks);
        networkService.remove(networkName);
    }
}