package com.saldium.saldium.controller;

import com.saldium.saldium.dto.CategoriaRequestDTO;
import com.saldium.saldium.dto.CategoriaResponseDTO;
import com.saldium.saldium.service.CategoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categorias")
@RequiredArgsConstructor
public class CategoriaController {
    private final CategoriaService categoriaService;

    @PostMapping
    public ResponseEntity<CategoriaResponseDTO> save(@Valid @RequestBody CategoriaRequestDTO categoriaRequestDTO) {
        CategoriaResponseDTO response = categoriaService.save(categoriaRequestDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<CategoriaResponseDTO> findAll() {
        return categoriaService.findAll();
    }

    @GetMapping("/{id}")
    public CategoriaResponseDTO findById(@PathVariable Long id) {
        return categoriaService.findById(id);
    }

    @PutMapping("/{id}")
    public CategoriaResponseDTO update(@PathVariable Long id, @Valid @RequestBody CategoriaRequestDTO categoriaRequestDTO) {
        return categoriaService.update(id, categoriaRequestDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoriaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
