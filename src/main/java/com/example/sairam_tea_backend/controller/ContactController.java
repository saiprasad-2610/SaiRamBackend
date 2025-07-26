package com.example.sairam_tea_backend.controller;

import com.example.sairam_tea_backend.dto.ContactFormDTO;
import com.example.sairam_tea_backend.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ContactController {

    @Autowired
    private MailService mailService;

    @PostMapping("/contact/submit")
    public ResponseEntity<?> sendContactMessage(@RequestBody ContactFormDTO contactForm) {
        mailService.sendContactEmail(contactForm);
        return ResponseEntity.ok("Message sent");
    }



}