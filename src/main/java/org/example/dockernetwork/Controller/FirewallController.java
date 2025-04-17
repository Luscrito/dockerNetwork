package org.example.dockernetwork.Controller;

import lombok.extern.slf4j.Slf4j;
import org.example.dockernetwork.Service.FirewallService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/firewall")
public class FirewallController {

    @Autowired
    private FirewallService firewallService;

    @PostMapping("/set")
    public String setFirewall(
            @RequestParam String container1,
            @RequestParam String container2Ip,
            @RequestParam(defaultValue = "A") String op,
            @RequestParam(defaultValue = "INPUT") String direction
    ) {
        return firewallService.set(container1, container2Ip, op, direction);
    }
}

