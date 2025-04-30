package org.example.dockernetwork.Controller;

import lombok.extern.slf4j.Slf4j;
import org.example.dockernetwork.Service.FirewallService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/firewall")
public class FirewallController {

    @Autowired
    private FirewallService firewallService;

    // 添加防火墙规则
    @PostMapping("/add")
    public ResponseEntity<String> addRule(
            @RequestParam String containerName,
            @RequestParam String chain,
            @RequestParam String rule) {
        try {
            String result = firewallService.addRule(containerName, chain.toUpperCase(), rule);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("添加规则失败: " + e.getMessage());
        }
    }

    // 删除防火墙规则
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteRule(
            @RequestParam String containerName,
            @RequestParam String chain,
            @RequestParam String rule) {
        try {
            String result = firewallService.deleteRule(containerName, chain.toUpperCase(), rule);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("删除规则失败: " + e.getMessage());
        }
    }

    // 根据索引删除规则
    @DeleteMapping("/deleteByIndex")
    public ResponseEntity<String> deleteRuleByIndex(
            @RequestParam String containerName,
            @RequestParam String chain,
            @RequestParam int index) {
        try {
            String result = firewallService.deleteRuleByIndex(containerName, chain.toUpperCase(), index);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("删除规则失败: " + e.getMessage());
        }
    }

    // 清空所有防火墙规则
    @DeleteMapping("/clearAll")
    public ResponseEntity<String> clearAllRules(@RequestParam String containerName) {
        try {
            String result = firewallService.clearAllRules(containerName);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("清空规则失败: " + e.getMessage());
        }
    }

    // 列出当前防火墙规则
    @GetMapping("/list")
    public ResponseEntity<String> listRules(@RequestParam String containerName, @RequestParam String chain) {
        try {
            String result = firewallService.listRules(containerName, chain.toUpperCase());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("获取规则失败: " + e.getMessage());
        }
    }
}

