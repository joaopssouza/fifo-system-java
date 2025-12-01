package com.joaopssouza.fifosystem.service;

import com.joaopssouza.fifosystem.domain.repository.PackageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QrCodeServiceTest {

    @Mock
    private PackageRepository packageRepository;

    @InjectMocks
    private QrCodeService qrCodeService;

    @Test
    @DisplayName("Deve gerar IDs sequenciais corretamente quando o banco está vazio")
    void shouldGenerateSequentialIdsWhenDbIsEmpty() {
        // Arrange
        int quantity = 3;
        // Mock: Nenhum ID existe
        when(packageRepository.existsByTrackingIdGlobally(anyString())).thenReturn(false);

        // Act
        List<String> result = qrCodeService.generateData(quantity);

        // Assert
        assertEquals(3, result.size());
        assertEquals("CG000001", result.get(0));
        assertEquals("CG000002", result.get(1));
        assertEquals("CG000003", result.get(2));
    }

    @Test
    @DisplayName("Deve pular IDs existentes para garantir unicidade (Collision Handling)")
    void shouldSkipExistingIdsToEnsureUniqueness() {
        // Arrange
        int quantity = 2; // Queremos 2 novos códigos

        // Cenário:
        // CG000001 -> JÁ EXISTE (deve pular)
        // CG000002 -> Disponível (deve pegar)
        // CG000003 -> JÁ EXISTE (deve pular)
        // CG000004 -> Disponível (deve pegar)

        when(packageRepository.existsByTrackingIdGlobally("CG000001")).thenReturn(true);
        when(packageRepository.existsByTrackingIdGlobally("CG000002")).thenReturn(false);
        when(packageRepository.existsByTrackingIdGlobally("CG000003")).thenReturn(true);
        when(packageRepository.existsByTrackingIdGlobally("CG000004")).thenReturn(false);

        // Act
        List<String> result = qrCodeService.generateData(quantity);

        // Assert
        assertEquals(2, result.size()); // Garante que retornou a quantidade pedida
        assertEquals("CG000002", result.get(0)); // O primeiro válido
        assertEquals("CG000004", result.get(1)); // O segundo válido

        // Verify: Confirma que o serviço consultou o banco para verificar a existência
        verify(packageRepository).existsByTrackingIdGlobally("CG000001");
        verify(packageRepository).existsByTrackingIdGlobally("CG000003");
    }
}