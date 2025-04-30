package org.example.dockernetwork.Controller;

import org.example.dockernetwork.Service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private TestService testService;

    @PostMapping("/http")
    public String httpTest(@RequestParam String container1,
                           @RequestParam String container2Ip) {
        return testService.HttpTest(container1, container2Ip);
    }

    @PostMapping("/tcp")
    public String tcpTest(@RequestParam String container1,
                          @RequestParam String container2,
                          @RequestParam String container2Ip,
                          @RequestParam int size,
                          @RequestParam int time) {
        return testService.TCPTest(container1, container2, container2Ip, size, time);
    }

    @PostMapping("/udp")
    public String udpTest(@RequestParam String container1,
                          @RequestParam String container2,
                          @RequestParam String container2Ip,
                          @RequestParam String velocity,
                          @RequestParam int size) {
        return testService.UDPTest(container1, container2, container2Ip, velocity, size);
    }

    @PostMapping("/netcat")
    public String netcatTest(@RequestParam String container1,
                             @RequestParam String container2Ip) {
        return testService.NetcatTest(container1, container2Ip);
    }

    @PostMapping("/ping")
    public String pingTest(@RequestParam String container1,
                           @RequestParam String container2Ip,
                           @RequestParam int size,
                           @RequestParam int num) {
        return testService.PingTest(container1, container2Ip, size, num);
    }
}
