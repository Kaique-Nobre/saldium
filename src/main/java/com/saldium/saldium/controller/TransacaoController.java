package com.saldium.saldium.controller;

import com.saldium.saldium.dto.transacao.TransacaoRequestDTO;
import com.saldium.saldium.dto.transacao.TransacaoResponseDTO;
import com.saldium.saldium.service.TransacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transacoes")
@RequiredArgsConstructor
public class TransacaoController {
    private final TransacaoService transacaoService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TransacaoResponseDTO> save(@Valid @RequestBody TransacaoRequestDTO transacaoRequestDTO) {
        TransacaoResponseDTO transacao = transacaoService.save(transacaoRequestDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(transacao);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<TransacaoResponseDTO> findAll() {
        return transacaoService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public TransacaoResponseDTO findById(@PathVariable Long id) {
        return transacaoService.findById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public TransacaoResponseDTO update(@PathVariable Long id, @Valid @RequestBody TransacaoRequestDTO transacaoRequestDTO) {
        return transacaoService.update(id, transacaoRequestDTO);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        transacaoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
