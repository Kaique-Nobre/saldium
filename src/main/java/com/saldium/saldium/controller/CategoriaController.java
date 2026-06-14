package com.saldium.saldium.controller;

import com.saldium.saldium.dto.categoria.CategoriaRequestDTO;
import com.saldium.saldium.dto.categoria.CategoriaResponseDTO;
import com.saldium.saldium.service.CategoriaService;
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

@Tag(name = "categorias")
@RestController
@RequestMapping("/categorias")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class CategoriaController {
    private final CategoriaService categoriaService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cria uma categoria", description = "Usuários podem criar categorias próprias, elas não podem ser iguais a do sistema. Categorias criadas por Admin são consideradas categorias do sistema")
    public ResponseEntity<CategoriaResponseDTO> save(@Valid @RequestBody CategoriaRequestDTO categoriaRequestDTO) {
        CategoriaResponseDTO response = categoriaService.save(categoriaRequestDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lista todas as categorias", description = "Usuário: Pode ver apenas as próprias categorias e as do sistema. Admin: Retorna TODAS as categorias")
    public List<CategoriaResponseDTO> findAll() {
        return categoriaService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Busca categorias por ID", description = "Usuário: pode procurar apenas as próprias categorias, ou as do sistema, caso tente buscar categoria de outro usuário retornará NOT_FOUND. Admin: Pode buscar qualquer categoria")
    public CategoriaResponseDTO findById(@PathVariable Long id) {
        return categoriaService.findById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Atualiza categoria", description = "Usuário: Pode atualizar apenas as próprias categorias, uma categoria que está sendo usada em alguma transação não pode ser alterada. Admin: Pode atualizar todas as categorias")
    public CategoriaResponseDTO update(@PathVariable Long id, @Valid @RequestBody CategoriaRequestDTO categoriaRequestDTO) {
        return categoriaService.update(id, categoriaRequestDTO);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Deleta categoria", description = "Usuário: Pode deletar apenas as próprias categorias, uma categoria que está sendo usada em alguma transação não pode ser deletada. Admin: Pode deletar todas as categorias")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoriaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
