package org.example.dockernetwork.Service;

import org.springframework.http.ResponseEntity;

public interface BirdService {

    public ResponseEntity<String> configure(String containerName);
}
