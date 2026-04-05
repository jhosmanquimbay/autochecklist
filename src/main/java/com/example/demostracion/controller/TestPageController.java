package com.example.demostracion.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestPageController {

    @GetMapping("/test-email")
    public String testEmailPage() {
        return "test-email";
    }
}