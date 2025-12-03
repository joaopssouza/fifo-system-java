package com.joaopssouza.fifosystem.controller;

import com.joaopssouza.fifosystem.domain.entity.ProductPackage;
import com.joaopssouza.fifosystem.domain.repository.PackageRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Métricas e Estatísticas")
public class DashboardController {

    private final PackageRepository packageRepository;

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        // --- ALTERAÇÃO AQUI ---
        // Busca apenas itens ativos na fila (ignora PENDENTE e Soft Deleted)
        List<ProductPackage> allPackages = packageRepository.findByBufferNotOrderByEntryTimestampAsc("PENDENTE");
        
        LocalDateTime now = LocalDateTime.now();

        // Variáveis de contagem
        long backlogCount = 0;
        long backlogValue = 0;
        
        Map<String, Long> counts = new HashMap<>(Map.of("RTS", 0L, "EHA", 0L, "SAL", 0L));
        Map<String, Long> values = new HashMap<>(Map.of("RTS", 0L, "EHA", 0L, "SAL", 0L));
        Map<String, Double> avgTimes = new HashMap<>(Map.of("RTS", 0.0, "EHA", 0.0));
        Map<String, Long> totalSeconds = new HashMap<>(Map.of("RTS", 0L, "EHA", 0L));

        for (ProductPackage pkg : allPackages) {
            String buf = pkg.getBuffer();
            int val = pkg.getProfileValue() != null ? pkg.getProfileValue() : 0;

            // Incrementa contagens por buffer
            if (counts.containsKey(buf)) {
                counts.put(buf, counts.get(buf) + 1);
                values.put(buf, values.get(buf) + val);
            }

            // Backlog Geral (Exclui SAL)
            // (PENDENTE já foi filtrado na query inicial)
            if (!"SAL".equals(buf)) {
                backlogCount++;
                backlogValue += val;

                // Cálculo de tempo para média (apenas RTS e EHA)
                if (pkg.getEntryTimestamp() != null) {
                    long seconds = Duration.between(pkg.getEntryTimestamp(), now).getSeconds();
                    if (seconds > 0) {
                        totalSeconds.put(buf, totalSeconds.getOrDefault(buf, 0L) + seconds);
                    }
                }
            }
        }

        // Finaliza médias
        if (counts.get("RTS") > 0) avgTimes.put("RTS", (double) totalSeconds.get("RTS") / counts.get("RTS"));
        if (counts.get("EHA") > 0) avgTimes.put("EHA", (double) totalSeconds.get("EHA") / counts.get("EHA"));

        return ResponseEntity.ok(Map.of(
            "backlogCount", backlogCount,
            "backlogValue", backlogValue,
            "counts", counts,
            "values", values,
            "avgTimes", avgTimes
        ));
    }
}