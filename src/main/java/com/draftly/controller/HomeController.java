package com.draftly.controller;

import com.draftly.model.UserRole;
import com.draftly.security.RequireRoles;
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
    @RequireRoles({UserRole.AUTHOR, UserRole.REVIEWER, UserRole.ADMIN})
    public Map<String, String> home() {
        return Map.of(
            "application", "Draftly",
            "description", "Academic Research Assistance Platform",
            "version", "0.0.1"
        );
    }
}
