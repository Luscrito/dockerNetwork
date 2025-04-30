package org.example.dockernetwork;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.example.dockernetwork.Mapper")
public class DockerNetworkApplication {

    public static void main(String[] args) {
        SpringApplication.run(DockerNetworkApplication.class, args);
    }

}
