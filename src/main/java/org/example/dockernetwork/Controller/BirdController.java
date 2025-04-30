package org.example.dockernetwork.Controller;

import lombok.extern.slf4j.Slf4j;
import org.example.dockernetwork.Entity.StaticRouteEntity;
import org.example.dockernetwork.Service.BirdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@Slf4j
@RequestMapping("/bird")
public class BirdController {

    @Autowired
    private BirdService birdService;

    @PostMapping("/active")
    public ResponseEntity<String> activeConfigure(@RequestParam String containerName) {
        return birdService.activeConfigure(containerName);
    }

    @PostMapping("/actives")
    public ResponseEntity<String> activesConfigure(@RequestBody List<String> containerNames) {
        for(String containerName : containerNames) {
            birdService.activeConfigure(containerName);
        }
        return ResponseEntity.ok("Active configured");
    }

    @PostMapping("/static")
    public ResponseEntity<String> staticConfigure(@RequestParam String containerName, @RequestBody List<StaticRouteEntity> routeList) {
        return birdService.staticConfigure(containerName, routeList);
    }

    @PostMapping("/add")
    public ResponseEntity<String> addStaticRoutes(
            @RequestParam String containerName,
            @RequestBody List<StaticRouteEntity> routes
    ) {
        return birdService.addStaticRoutes(containerName, routes);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> removeRoutes(
            @RequestParam String containerName,
            @RequestBody List<StaticRouteEntity> routes) {
        return birdService.removeStaticRoutes(containerName, routes);
    }

    @PostMapping("/reset")
    public ResponseEntity<String> resetBirdConfig(@RequestParam String containerName) {
        return birdService.resetBirdConfig(containerName);
    }

    @GetMapping("/list")
    public List<StaticRouteEntity> getStaticRoutes(String containerName) {
        return birdService.getStaticRoutes(containerName);
    }

}