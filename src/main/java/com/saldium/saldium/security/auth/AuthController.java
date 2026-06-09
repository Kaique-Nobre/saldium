package com.saldium.saldium.security.auth;

import com.saldium.saldium.security.auth.dto.*;
import com.saldium.saldium.security.token.RefreshTokenRequestDTO;
import com.saldium.saldium.security.token.RefreshTokenResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/cadastro")
    public ResponseEntity<Void> cadastrar(@Valid @RequestBody CadastroDTO request) {
        authService.cadastrar(request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public LoginResponseDTO login(@Valid @RequestBody LoginRequestDTO request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public RefreshTokenResponseDTO refreshToken(@RequestBody RefreshTokenRequestDTO request) {
        return authService.refreshToken(request);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequestDTO request) {
        authService.logout(request);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/alterar-senha")
    public ResponseEntity<Void> alterarSenha(@Valid @RequestBody AlterarSenhaRequestDTO request) {
        authService.alterarSenha(request);
        return ResponseEntity.ok().build();
    }
}
