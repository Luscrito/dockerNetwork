package org.example.dockernetwork.Service;

import org.example.dockernetwork.Entity.StaticRouteEntity;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;

public interface BirdService {

    ResponseEntity<String> activeConfigure(String containerName);

    ResponseEntity<String> staticConfigure(String containerName, List<StaticRouteEntity> routeList);

    ResponseEntity<String> addStaticRoutes(String containerName, List<StaticRouteEntity> routeList);

    ResponseEntity<String> removeStaticRoutes(String containerName, List<StaticRouteEntity> routeList);

    ResponseEntity<String> resetBirdConfig(String containerName);

    List<StaticRouteEntity> getStaticRoutes(String containerName);

}
