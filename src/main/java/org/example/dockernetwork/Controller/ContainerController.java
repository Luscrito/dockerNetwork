package org.example.dockernetwork.Controller;

import lombok.extern.slf4j.Slf4j;
import org.example.dockernetwork.Entity.DockerContainer;
import org.example.dockernetwork.Service.BirdService;
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

    @Autowired
    private BirdService birdService;


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

    @DeleteMapping("/delete2/{containerName}")
    public void removeContainerByName2(@PathVariable String containerName) {
        Map<String, String> Networks = containerService.getIp(containerName);
        Set<String> NetworksSet = Networks.keySet();
        containerService.remove(containerName);
        for(String NetworkName:NetworksSet){
            networkService.disconnectAllContainers(NetworkName);
            networkService.remove(NetworkName);
        }
    }

    @DeleteMapping("/delete3")
    public void removeContainerByName3(@RequestBody List<String> containers) {
        for(String containerName:containers){
            removeContainerByName2(containerName);
        }
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
            networkName = container1+"_"+container2;
        }
        else
            networkName = container2+"_"+container1;
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
            networkName = container1+"_"+container2;
        }
        else
            networkName = container2+"_"+container1;
        System.out.println(networkName);
        List<String> networks = new ArrayList<>();
        networks.add(networkName);
        containerService.disconnect(container1, networks);
        containerService.disconnect(container2, networks);
        networkService.remove(networkName);
    }

    @PostMapping("/disconnect2")
    public void disconnect2Containers(@RequestParam String container1, @RequestParam String container2) {
        Set<String> Networks1 = containerService.getIp(container1).keySet();
        Set<String> Networks2 = containerService.getIp(container2).keySet();
        List<String> networks = new ArrayList<>();
        for(String Network:Networks1){
            if(Networks2.contains(Network)){
                networks.add(Network);
            }
        }
        containerService.disconnect(container1, networks);
        containerService.disconnect(container2, networks);
        for(String Network:networks){
            networkService.disconnectAllContainers(Network);
            networkService.remove(Network);
        }
    }
}