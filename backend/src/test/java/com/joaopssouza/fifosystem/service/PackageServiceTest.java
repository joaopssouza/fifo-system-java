package com.joaopssouza.fifosystem.service;

import com.joaopssouza.fifosystem.domain.entity.ProductPackage;
import com.joaopssouza.fifosystem.domain.repository.PackageRepository;
import com.joaopssouza.fifosystem.dto.PackageEntryRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PackageServiceTest {

    @Mock
    private PackageRepository packageRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private PackageService packageService;

    @Test
    @DisplayName("Deve registrar entrada com sucesso quando dados são válidos")
    void shouldRegisterEntrySuccessfully() {
        // 1. Arrange (Preparar o cenário)
        PackageEntryRequest request = new PackageEntryRequest("CG001", "RTS", "RUA-01", "P");
        
        // CORREÇÃO: Mockamos o novo método que o Service usa (findByTrackingIdGlobally)
        // Retorna null para simular que o pacote NUNCA existiu
        when(packageRepository.findByTrackingIdGlobally("CG001")).thenReturn(null);
        
        // Mock do save para retornar o objeto salvo
        when(packageRepository.save(any(ProductPackage.class))).thenAnswer(invocation -> {
            ProductPackage p = invocation.getArgument(0);
            p.setId(1L); // Simula o ID gerado pelo banco
            p.setTrackingId("CG001");
            p.setProfileType("P");
            p.setProfileValue(250);
            return p;
        });

        // 2. Act (Executar a ação)
        ProductPackage result = packageService.registerEntry(request);

        // 3. Assert (Verificar o resultado)
        assertNotNull(result);
        assertEquals("CG001", result.getTrackingId());
        assertEquals(250, result.getProfileValue());
        
        // Verifica se o log de auditoria foi chamado
        verify(auditService, times(1)).logAction(eq("ENTRADA"), contains("CG001"));
    }

    @Test
    @DisplayName("Não deve permitir Tracking ID duplicado (Item Ativo)")
    void shouldThrowErrorWhenDuplicateTrackingId() {
        // 1. Arrange
        PackageEntryRequest request = new PackageEntryRequest("CG001", "RTS", "RUA-01", "P");
        
        // CORREÇÃO: Criamos um pacote existente e ATIVO (deletedAt = null)
        ProductPackage existingPkg = new ProductPackage();
        existingPkg.setTrackingId("CG001");
        existingPkg.setDeletedAt(null); // Importante: null significa ativo
        
        // O Mock retorna esse pacote existente
        when(packageRepository.findByTrackingIdGlobally("CG001")).thenReturn(existingPkg);

        // 2. Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            packageService.registerEntry(request);
        });

        assertEquals("O Tracking ID CG001 já está na fila.", exception.getMessage());

        // 3. Verify (Garante que NADA foi salvo e NENHUM log foi gerado)
        verify(packageRepository, never()).save(any());
        verify(auditService, never()).logAction(anyString(), anyString());
    }
}