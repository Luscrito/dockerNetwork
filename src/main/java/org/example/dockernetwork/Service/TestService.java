package org.example.dockernetwork.Service;

import org.springframework.web.bind.annotation.RequestParam;

public interface TestService {

    String HttpTest(@RequestParam String container1, @RequestParam String container2Ip);

    String TCPTest(@RequestParam String container1,
                          @RequestParam String container2,
                          @RequestParam String container2Ip,
                          @RequestParam int size,
                          @RequestParam int time);

    String UDPTest(@RequestParam String container1,
                          @RequestParam String container2,
                          @RequestParam String container2Ip,
                          @RequestParam String velocity,
                          @RequestParam int size);

    String NetcatTest(@RequestParam String container1, @RequestParam String container2Ip);

    String PingTest(@RequestParam String container1,
                           @RequestParam String container2Ip,
                           @RequestParam int size,
                           @RequestParam int num);
}
