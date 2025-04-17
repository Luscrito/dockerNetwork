package org.example.dockernetwork.Controller;

import lombok.extern.slf4j.Slf4j;
import org.example.dockernetwork.Service.ParamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/parameter")
public class ParameterController {

    @Autowired
    private ParamService paramService;

    @PostMapping("/bandwidth")
    public void limitBandwidth(@RequestParam String container,
                               @RequestParam String bandwidthLimitInKbps,
                               @RequestParam String burst,
                               @RequestParam int latency) {
        paramService.setBandwidth(container, bandwidthLimitInKbps, burst, latency);
    }

    @PostMapping("/delay")
    public void setDelay(@RequestParam String container, @RequestParam int delay) {
        paramService.setDelay(container, delay);
    }

    @PostMapping("/loss")
    public void setLoss(@RequestParam String container, @RequestParam int loss) {
        paramService.setPacketLoss(container, loss);
    }

    @PostMapping("/combined")
    public void setAll(@RequestParam String container,
                       @RequestParam String netInterface,
                       @RequestParam int delay,
                       @RequestParam int loss) {
        paramService.setProperties(container, netInterface, loss, delay);
    }

}
