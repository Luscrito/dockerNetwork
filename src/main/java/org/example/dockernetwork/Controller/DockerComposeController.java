package org.example.dockernetwork.Controller;

import lombok.extern.slf4j.Slf4j;
import org.example.dockernetwork.Entity.DockerContainer;
import org.example.dockernetwork.Entity.DockerNetwork;
import org.example.dockernetwork.Service.DockerComposeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/dockerCompose")
public class DockerComposeController {

    @Autowired
    private DockerComposeService dockerComposeService;

    @PostMapping("/create")
    public void createDockerCompose(@RequestBody List<DockerNetwork> dockerNetworkList, @RequestBody List<DockerContainer> dockerContainerList, @RequestParam(defaultValue = "docker-compose.yml") String file) {
        dockerComposeService.create(dockerNetworkList, dockerContainerList, file);
    }
}
