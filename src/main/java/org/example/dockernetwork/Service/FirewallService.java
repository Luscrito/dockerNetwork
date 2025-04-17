package org.example.dockernetwork.Service;

public interface FirewallService {

    public String set(String container1, String container2Ip, String op, String direction);
}
