package com.bathforge.controller.admin;

import org.springframework.web.bind.annotation.*;

/**
 * Controller for home and health check endpoints.
 */
@RestController
@RequestMapping("/api")
public class HomeController {

    /**
     * Returns a welcome message for the API.
     *
     * @return welcome message string
     */
    @GetMapping("/")
    public String home() {
        return "Welcome to BathForge API!";
    }

    /**
     * Health check endpoint to verify the API is running.
     *
     * @return status string indicating the API is operational
     */
    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}
