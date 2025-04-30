package org.example.dockernetwork.Controller;

import lombok.extern.slf4j.Slf4j;
import org.example.dockernetwork.Service.ParamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/parameter")
public class ParameterController {

    @Autowired
    private ParamService paramService;

    @GetMapping("/get")
    public String getParameter(@RequestParam String containerName) {
        return paramService.readProperties(containerName);
    }

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
                       @RequestParam(defaultValue = "100G") String bandwidth,
                       @RequestParam(defaultValue = "0") int delay,
                       @RequestParam(defaultValue = "0") int loss) {
        paramService.setProperties(container, bandwidth, loss, delay);
    }

    @PostMapping("/update")
    public ResponseEntity<String> updateResources(
            @RequestParam String containerName,
            @RequestParam(required = false) Long memory,         // 单位：字节
            @RequestParam(required = false) Long memorySwap,     // 单位：字节
            @RequestParam(required = false) Integer cpuQuota,       // 比如 50000 表示 50% 的 CPU
            @RequestParam(required = false) Integer cpuPeriod,      // 默认 100000（100ms），可以保持默认
            @RequestParam(required = false) Integer cpuShares    // 默认 1024
    ) {
        return paramService.updateContainerResources(
                containerName, memory, memorySwap, cpuQuota, cpuPeriod, cpuShares
        );
    }
}
