package com.draftly.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Home Controller - Health check / API info.
 */
@RestController
@RequestMapping("/api")
public class HomeController {

    @GetMapping
    public Map<String, String> home() {
        return Map.of(
            "application", "Draftly",
            "description", "Academic Research Assistance Platform",
            "version", "0.0.1"
        );
    }
}
