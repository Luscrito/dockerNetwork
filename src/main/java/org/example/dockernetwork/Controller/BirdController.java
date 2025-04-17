package org.example.dockernetwork.Controller;

import lombok.extern.slf4j.Slf4j;
import org.example.dockernetwork.Service.BirdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.*;


@RestController
@Slf4j
@RequestMapping("/bird")
public class BirdController {

    @Autowired
    private BirdService birdService;

    @PostMapping("/configure")
    public ResponseEntity<String> configureBird(@RequestParam String containerName) {
        return birdService.configure(containerName);
    }
}