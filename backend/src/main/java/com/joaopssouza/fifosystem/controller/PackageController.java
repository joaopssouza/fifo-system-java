package com.joaopssouza.fifosystem.controller;

import com.joaopssouza.fifosystem.domain.entity.ProductPackage;
import com.joaopssouza.fifosystem.dto.PackageEntryRequest;
import com.joaopssouza.fifosystem.dto.PackageMoveRequest;
import com.joaopssouza.fifosystem.service.PackageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/packages")
@RequiredArgsConstructor
public class PackageController {

    private final PackageService packageService;
    // Removemos WebSocketHandler e DashboardController daqui

    @GetMapping
    public List<ProductPackage> getAllPackages() {
        return packageService.findAll();
    }

    @PostMapping("/entry")
    public ResponseEntity<?> entryPackage(@Valid @RequestBody PackageEntryRequest request) {
        try {
            ProductPackage savedPackage = packageService.registerEntry(request);
            // Sem broadcast
            return ResponseEntity.ok(savedPackage);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> exitPackage(@PathVariable Long id) {
        try {
            packageService.registerExit(id);
            // Sem broadcast
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/move")
    public ResponseEntity<?> movePackage(@PathVariable Long id, @Valid @RequestBody PackageMoveRequest request) {
        try {
            ProductPackage updatedPackage = packageService.registerMove(id, request.rua());
            // Sem broadcast
            return ResponseEntity.ok(updatedPackage);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/exit")
    public ResponseEntity<?> exitPackageByTrackingId(@RequestBody Map<String, String> body) {
        String trackingId = body.get("trackingId");
        if (trackingId == null) return ResponseEntity.badRequest().body("Tracking ID obrigatório");

        var pkg = packageService.findByTrackingIdGlobally(trackingId);
        
        if (pkg == null) return ResponseEntity.notFound().build();
        if (pkg.getDeletedAt() != null) return ResponseEntity.badRequest().body("Item já saiu da fila.");

        packageService.registerExit(pkg.getId());
        // Sem broadcast
        return ResponseEntity.ok().body(Map.of("message", "Saída registrada."));
    }
}