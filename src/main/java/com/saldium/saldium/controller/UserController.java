package com.saldium.saldium.controller;

import com.saldium.saldium.dto.user.UserResponseDTO;
import com.saldium.saldium.security.auth.dto.DeletarContaRequestDTO;
import com.saldium.saldium.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "usuario")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    private final UserService userService;

    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Deleta a conta do usuário")
    public ResponseEntity<Void> deletarUsuario(@RequestBody DeletarContaRequestDTO request) {
        userService.deletarUsuario(request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Retorna informações do usuário", description = "Retorna: nome, email, data de criação da conta")
    public UserResponseDTO getUsuarioInfo() {
        return userService.getUserInfo();
    }
}
