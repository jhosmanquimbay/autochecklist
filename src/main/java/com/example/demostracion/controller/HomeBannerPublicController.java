package com.example.demostracion.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demostracion.dto.HomeBannerForm;
import com.example.demostracion.service.HomeBannerService;

@RestController
public class HomeBannerPublicController {

    private final HomeBannerService homeBannerService;

    public HomeBannerPublicController(HomeBannerService homeBannerService) {
        this.homeBannerService = homeBannerService;
    }

    @GetMapping(value = "/home-banners.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<HomeBannerForm> listarActivos() {
        return homeBannerService.listarActivos();
    }
}