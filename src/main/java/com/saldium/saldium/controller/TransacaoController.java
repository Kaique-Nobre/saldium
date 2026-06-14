package com.saldium.saldium.controller;

import com.saldium.saldium.dto.transacao.TransacaoRequestDTO;
import com.saldium.saldium.dto.transacao.TransacaoResponseDTO;
import com.saldium.saldium.service.TransacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "transações")
@RestController
@RequestMapping("/transacoes")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class TransacaoController {
    private final TransacaoService transacaoService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cria uma transação")
    public ResponseEntity<TransacaoResponseDTO> save(@Valid @RequestBody TransacaoRequestDTO transacaoRequestDTO) {
        TransacaoResponseDTO transacao = transacaoService.save(transacaoRequestDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(transacao);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lista todas as transações", description = "Usuário: Pode ver apenas as próprias transações. Admin: Pode ver todas as transações")
    public List<TransacaoResponseDTO> findAll() {
        return transacaoService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Busca transação por ID", description = "Usuário: Pode buscar apenas as próprias transações, caso busque a de outro usuário retornará NOT_FOUND. Admin: Pode buscar qualquer transação")
    public TransacaoResponseDTO findById(@PathVariable Long id) {
        return transacaoService.findById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Atualiza uma transação")
    public TransacaoResponseDTO update(@PathVariable Long id, @Valid @RequestBody TransacaoRequestDTO transacaoRequestDTO) {
        return transacaoService.update(id, transacaoRequestDTO);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Deleta uma transação")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        transacaoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
