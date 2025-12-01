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

@RestController
@RequestMapping("/api/packages")
@RequiredArgsConstructor
public class PackageController {

    private final PackageService packageService;

    @GetMapping
    public List<ProductPackage> getAllPackages() {
        return packageService.findAll();
    }

    @PostMapping("/entry")
    public ResponseEntity<?> entryPackage(@Valid @RequestBody PackageEntryRequest request) {
        try {
            ProductPackage savedPackage = packageService.registerEntry(request);
            return ResponseEntity.ok(savedPackage);
        } catch (IllegalArgumentException e) {
            // Retorna Bad Request (400) se a regra de negócio falhar
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> exitPackage(@PathVariable Long id) {
        try {
            packageService.registerExit(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/move")
    public ResponseEntity<?> movePackage(@PathVariable Long id, @Valid @RequestBody PackageMoveRequest request) {
        try {
            ProductPackage updatedPackage = packageService.registerMove(id, request.rua());
            return ResponseEntity.ok(updatedPackage);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/exit")
    public ResponseEntity<?> exitPackageByTrackingId(@RequestBody java.util.Map<String, String> body) {
        String trackingId = body.get("trackingId");
        if (trackingId == null) return ResponseEntity.badRequest().body("Tracking ID obrigatório");

        // --- CORREÇÃO: Usar packageService em vez de packageRepository ---
        var pkg = packageService.findByTrackingIdGlobally(trackingId);
        
        if (pkg == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (pkg.getDeletedAt() != null) {
             return ResponseEntity.badRequest().body("Item já saiu da fila.");
        }

        packageService.registerExit(pkg.getId());
        return ResponseEntity.ok().body(java.util.Map.of("message", "Saída registrada."));
    }

    // No PackageController.java
    @GetMapping("/dashboard/stats")
    public ResponseEntity<?> getDashboardStats() {
        // Implementar lógica de contagem aqui se necessário,
        // ou retornar a lista completa em /api/packages e deixar o React contar.
        return ResponseEntity.ok(packageService.findAll());
    }
}