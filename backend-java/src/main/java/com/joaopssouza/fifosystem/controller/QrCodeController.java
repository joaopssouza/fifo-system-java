package com.joaopssouza.fifosystem.controller;

import com.joaopssouza.fifosystem.service.QrCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/qrcodes")
@RequiredArgsConstructor
public class QrCodeController {

    private final QrCodeService qrCodeService;

    @PostMapping("/generate-data")
    public ResponseEntity<?> generateData(@RequestBody Map<String, Integer> body) {
        int quantity = body.getOrDefault("quantity", 0);
        if (quantity <= 0) return ResponseEntity.badRequest().body("Quantidade inválida.");
        
        return ResponseEntity.ok(Map.of("data", qrCodeService.generateData(quantity)));
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirmData(@RequestBody Map<String, List<String>> body) {
        List<String> trackingIds = body.get("trackingIds");
        if (trackingIds == null || trackingIds.isEmpty()) return ResponseEntity.badRequest().build();

        qrCodeService.confirmData(trackingIds);
        return ResponseEntity.ok(Map.of("message", "Códigos confirmados."));
    }

    @GetMapping("/find/{trackingId}")
    public ResponseEntity<?> findData(@PathVariable String trackingId) {
        try {
            String found = qrCodeService.findData(trackingId);
            return ResponseEntity.ok(Map.of("data", List.of(found)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}