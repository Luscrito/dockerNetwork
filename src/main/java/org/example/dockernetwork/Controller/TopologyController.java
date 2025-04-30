package org.example.dockernetwork.Controller;

import lombok.extern.slf4j.Slf4j;
import org.example.dockernetwork.Service.TopologyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/topology")
public class TopologyController {

    @Autowired
    private TopologyService topologyService;

    @GetMapping("/get")
    public Map<String, Object> getTopology() {
        return topologyService.getTopology();
    }

}
