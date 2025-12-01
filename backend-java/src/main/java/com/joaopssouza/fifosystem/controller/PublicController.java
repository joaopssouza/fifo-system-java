package com.joaopssouza.fifosystem.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

@RestController
@RequestMapping("/public")
public class PublicController {

    @GetMapping("/time")
    public ResponseEntity<?> getServerTime() {
        // Retorna a hora atual no formato que o JS entende (ISO 8601)
        String serverTime = LocalDateTime.now(ZoneId.of("America/Sao_Paulo")).toString();
        return ResponseEntity.ok(Map.of("serverTime", serverTime));
    }
}