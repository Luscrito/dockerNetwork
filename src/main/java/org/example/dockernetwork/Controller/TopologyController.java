package org.example.dockernetwork.Controller;

import lombok.extern.slf4j.Slf4j;
import org.example.dockernetwork.Service.TopologyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/topology")
public class TopologyController {

    @Autowired
    private TopologyService topologyService;
    @GetMapping("/create")

    public String createTopology(@RequestParam(defaultValue = "topology.dot") String file1, @RequestParam(defaultValue = "topology.png") String file2) {
        return topologyService.create(file1, file2);
    }

    @DeleteMapping("/delete")
    public String deleteTopology(@RequestParam(defaultValue = "topology.dot") String file1, @RequestParam(defaultValue = "topology.png") String file2) {
        return topologyService.delete(file1, file2);
    }

    @PutMapping("/update")
    public void updateTopology(@RequestParam(defaultValue = "topology.dot") String file1, @RequestParam(defaultValue = "topology.png") String file2) {
        deleteTopology(file1, file2);
        createTopology(file1, file2);
    }

}
