package com.example.demostracion.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demostracion.service.EmailService;
import com.example.demostracion.service.InboundMailService;

@Controller
@RequestMapping("/email-test")
public class EmailTestController {

    @Value("${spring.mail.username}")
    private String mailUsername;

    @Value("${spring.mail.password}")
    private String mailPassword;

    @Value("${mail.inbound.user}")
    private String imapUser;

    @Value("${mail.inbound.password}")
    private String imapPassword;

    private final EmailService emailService;
    private final InboundMailService inboundMailService;

    public EmailTestController(EmailService emailService, InboundMailService inboundMailService) {
        this.emailService = emailService;
        this.inboundMailService = inboundMailService;
    }

    @GetMapping
    public String testPage(Model model) {
        model.addAttribute("smtpUser", mailUsername);
        model.addAttribute("smtpPassword", maskPassword(mailPassword));
        model.addAttribute("imapUser", imapUser);
        model.addAttribute("imapPassword", maskPassword(imapPassword));
        return "email-test/test";
    }

    @PostMapping("/send")
    public String testSend(@RequestParam String to, @RequestParam String subject, 
                          @RequestParam String message, Model model) {
        try {
            emailService.sendEmail(to, subject, message, new org.springframework.web.multipart.MultipartFile[0]);
            model.addAttribute("success", "Email enviado exitosamente a " + to);
        } catch (Exception e) {
            model.addAttribute("error", "Error enviando email: " + e.getMessage());
        }
        
        model.addAttribute("smtpUser", mailUsername);
        model.addAttribute("smtpPassword", maskPassword(mailPassword));
        model.addAttribute("imapUser", imapUser);
        model.addAttribute("imapPassword", maskPassword(imapPassword));
        
        return "email-test/test";
    }

    @PostMapping("/test-imap")
    public String testImap(Model model) {
        try {
            // Forzar un polling manual del InboundMailService
            inboundMailService.processInbox();
            model.addAttribute("success", "Polling IMAP ejecutado manualmente - revisa los logs y la bandeja de entrada");
        } catch (Exception e) {
            model.addAttribute("error", "Error probando IMAP: " + e.getMessage());
        }
        
        model.addAttribute("smtpUser", mailUsername);
        model.addAttribute("smtpPassword", maskPassword(mailPassword));
        model.addAttribute("imapUser", imapUser);
        model.addAttribute("imapPassword", maskPassword(imapPassword));
        
        return "email-test/test";
    }

    private String maskPassword(String password) {
        if (password == null || password.length() < 4) {
            return "****";
        }
        return password.substring(0, 2) + "****" + password.substring(password.length() - 2);
    }
}