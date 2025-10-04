package com.bathforge.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "Welcome to BathForge API!";
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}
