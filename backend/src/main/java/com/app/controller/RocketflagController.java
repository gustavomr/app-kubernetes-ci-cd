package com.app.controller;

import com.app.service.RocketflagService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/rocketflag")
public class RocketflagController {

    private final RocketflagService rocketflagService;

    public RocketflagController(RocketflagService rocketflagService) {
        this.rocketflagService = rocketflagService;
    }

    @GetMapping("/add-produto")
    public ResponseEntity<Map<String, Boolean>> status() {
        return ResponseEntity.ok(Map.of("enabled", rocketflagService.isFeatureEnabled()));
    }
}
