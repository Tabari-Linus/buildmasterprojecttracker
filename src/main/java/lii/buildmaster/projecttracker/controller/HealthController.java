package lii.buildmaster.projecttracker.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/")
    public Map<String, Object> home() {
        return Map.of(
                "application", "BuildMaster Project Tracker",
                "status", "UP",
                "timestamp", LocalDateTime.now(),
                "message", "Welcome to BuildMaster Project Tracker API!"
        );
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
                "status", "UP",
                "service", "BuildMaster Project Tracker"
        );
    }
}
