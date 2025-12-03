package com.joaopssouza.fifosystem.service;

import com.joaopssouza.fifosystem.domain.entity.ProductPackage;
import com.joaopssouza.fifosystem.domain.repository.PackageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QrCodeService {

    private final PackageRepository packageRepository;

    public List<String> generateData(int quantity) {
        List<String> nextIds = new ArrayList<>();
        int currentNumber = 1;

        while (nextIds.size() < quantity) {
            // Formata CG000001
            String nextIDStr = String.format("CG%06d", currentNumber);

            // Verifica se existe (incluindo deletados)
            if (!packageRepository.existsByTrackingIdGlobally(nextIDStr)) {
                nextIds.add(nextIDStr);
            }
            currentNumber++;
            
            // Safety break (opcional, para evitar loop infinito em banco cheio)
            if (currentNumber > 1000000) break;
        }
        return nextIds;
    }

    @Transactional
    public void confirmData(List<String> trackingIds) {
        List<ProductPackage> newPackages = trackingIds.stream()
                .map(id -> ProductPackage.builder()
                        .trackingId(id)
                        .buffer("PENDENTE")
                        .rua("INDEFINIDA")
                        .profileType("N/A")
                        .profileValue(0)
                        .build())
                .toList();

        packageRepository.saveAll(newPackages);
    }

    public String findData(String trackingId) {
        ProductPackage pkg = packageRepository.findByTrackingIdGlobally(trackingId);
        if (pkg == null) {
            throw new IllegalArgumentException("Código não encontrado.");
        }
        return pkg.getTrackingId();
    }
}