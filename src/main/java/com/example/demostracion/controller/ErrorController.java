package com.example.demostracion.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ErrorController {

    @GetMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Integer status = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        String message = (String) request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        String path = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        
        if (status != null) {
            model.addAttribute("status", status);
        }
        if (message != null) {
            model.addAttribute("message", message);
        }
        if (path != null) {
            model.addAttribute("path", path);
        }
        
        // Determinar la página a mostrar según el código de error
        if (status != null) {
            if (status == 404) {
                return "error/error_404";
            } else if (status == 500) {
                return "error/error_500";
            } else if (status == 403) {
                return "error/error_403";
            }
        }
        
        return "error/error_generic";
    }

    @GetMapping("/error/error_400")
    public String error400() {
        return "error/error_400"; // busca templates/error/error_400.html
    }
}

