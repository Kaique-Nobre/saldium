package com.saldium.saldium.security.auth;

import com.saldium.saldium.dto.email.ResendVerificationEmailRequestDTO;
import com.saldium.saldium.security.auth.dto.*;
import com.saldium.saldium.security.passwordResetToken.ForgotPasswordRequestDTO;
import com.saldium.saldium.security.passwordResetToken.ResetPasswordRequestDTO;
import com.saldium.saldium.security.refreshToken.RefreshTokenRequestDTO;
import com.saldium.saldium.security.refreshToken.RefreshTokenResponseDTO;
import com.saldium.saldium.security.verificationToken.VerificationTokenService;
import com.saldium.saldium.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Autenticação")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final VerificationTokenService verificationTokenService;

    @PostMapping("/cadastro")
    @Operation(summary = "Cadastra usuário", description = "Quando usuário se cadastra recebe um Email para autenticar próprio Email")
    public ResponseEntity<Void> cadastrar(@Valid @RequestBody CadastroDTO request) {
        authService.cadastrar(request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    @Operation(summary = "Login de usuário", description = "Quando usuário faz login recebe refresh token e access token")
    public LoginResponseDTO login(@Valid @RequestBody LoginRequestDTO request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Retorna um novo access token")
    public RefreshTokenResponseDTO refreshToken(@RequestBody RefreshTokenRequestDTO request) {
        return authService.refreshToken(request);
    }

    @PostMapping("/logout")
    @Operation(summary = "usuário faz logout", description = "Quando usuário faz logout, revoga refresh token")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequestDTO request) {
        authService.logout(request);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/alterar-senha")
    @Operation(summary = "usuário autenticado pode alterar senha", description = "Quando usuário altera a senha, revoga todos os seus refresh token")
    public ResponseEntity<Void> alterarSenha(@Valid @RequestBody AlterarSenhaRequestDTO request) {
        authService.alterarSenha(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/verify-email")
    @Operation(summary = "Verifica se email do usuário é valido")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        verificationTokenService.verifyEmail(token);
        return ResponseEntity.ok("Email verificado com sucesso");
    }

    @PostMapping("/resend-verification-email")
    @Operation(summary = "Reenvia email de verificação", description = "Caso o token de verificação de email do usuário tenha expirado,um novo email com um novo token será enviado ao usuário")
    public ResponseEntity<Void> resendVerificationEmail(@RequestBody @Valid ResendVerificationEmailRequestDTO request) {
        authService.resendVerificationEmail(
                request.email()
        );

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Usuário não autenticado pode solicitar alteração de senha", description = "Quando usuário esquece a senha, ele enviará seu email para que receba um link para alterar sua senha")
    public ResponseEntity<Void> forgotPassword(@RequestBody @Valid ForgotPasswordRequestDTO request) {
        authService.forgotPassword(request);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Usuário não autenticado pode alterar senha", description = "Após usuário solicitar alteração de senha e abrir link do email recebido, ele pode definir uma nova senha")
    public ResponseEntity<Void> resetPassword(
            @RequestParam String token,
            @RequestBody @Valid ResetPasswordRequestDTO request
    ) {

        authService.resetPassword(token, request);

        return ResponseEntity.noContent().build();
    }
}
