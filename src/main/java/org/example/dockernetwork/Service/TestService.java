package org.example.dockernetwork.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

@Service
public interface TestService {

    public String HttpTest(@RequestParam String container1, @RequestParam String container2Ip);

    public String TCPTest(@RequestParam String container1,
                          @RequestParam String container2,
                          @RequestParam String container2Ip,
                          @RequestParam int size,
                          @RequestParam int time);

    public String UDPTest(@RequestParam String container1,
                          @RequestParam String container2,
                          @RequestParam String container2Ip,
                          @RequestParam String velocity,
                          @RequestParam int size);

    public String NetcatTest(@RequestParam String container1, @RequestParam String container2Ip);

    public String PingTest(@RequestParam String container1,
                           @RequestParam String container2Ip,
                           @RequestParam int size);
}
