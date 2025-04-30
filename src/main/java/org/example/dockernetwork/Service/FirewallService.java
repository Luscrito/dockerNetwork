package org.example.dockernetwork.Service;

import java.io.IOException;

public interface FirewallService {
    String addRule(String containerName, String chain, String rule) throws IOException, InterruptedException;

    String deleteRule(String containerName, String chain, String rule) throws IOException, InterruptedException;

    String deleteRuleByIndex(String containerName, String chain, int index) throws IOException, InterruptedException;

    String clearAllRules(String containerName) throws IOException, InterruptedException;

    String listRules(String containerName, String chain) throws IOException, InterruptedException;

}

