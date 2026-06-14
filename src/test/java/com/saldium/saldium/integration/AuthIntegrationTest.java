package com.saldium.saldium.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saldium.saldium.dto.email.ResendVerificationEmailRequestDTO;
import com.saldium.saldium.security.auth.dto.AlterarSenhaRequestDTO;
import com.saldium.saldium.security.auth.dto.CadastroDTO;
import com.saldium.saldium.security.auth.dto.LoginRequestDTO;
import com.saldium.saldium.security.auth.dto.LogoutRequestDTO;
import com.saldium.saldium.security.jwt.JwtService;
import com.saldium.saldium.security.passwordResetToken.ForgotPasswordRequestDTO;
import com.saldium.saldium.security.passwordResetToken.PasswordResetToken;
import com.saldium.saldium.security.passwordResetToken.PasswordResetTokenRepository;
import com.saldium.saldium.security.passwordResetToken.ResetPasswordRequestDTO;
import com.saldium.saldium.security.refreshToken.RefreshToken;
import com.saldium.saldium.security.refreshToken.RefreshTokenRepository;
import com.saldium.saldium.security.refreshToken.RefreshTokenRequestDTO;
import com.saldium.saldium.security.user.Role;
import com.saldium.saldium.security.user.UserRepository;
import com.saldium.saldium.security.user.Usuario;
import com.saldium.saldium.security.verificationToken.VerificationTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static com.saldium.saldium.util.usuario.UsuarioCreator.criarUsuarioParaTesteDeIntegracao;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanDatabase() {
        passwordResetTokenRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        verificationTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void cadastrar_ShouldSaveUser_WhenSuccessfully() throws Exception {
        CadastroDTO request = new CadastroDTO("user", "user@email.com", "senha123");

        mockMvc.perform(post("/auth/cadastro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void cadastrar_ShouldReturnConflict_WhenEmailAlreadyRegistered() throws Exception {
        Usuario usuario = criarUsuarioParaTesteDeIntegracao();
        userRepository.save(usuario);

        CadastroDTO request = new CadastroDTO("user", "user@email.com", "senha123");

        mockMvc.perform(post("/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Email já registrado"));
    }

    @Test
    void login_ShouldReturnTokens_WhenSuccessfully() throws Exception {
        Usuario user = new Usuario();
        user.setNome("user");
        user.setEmail("user@email.com");
        user.setSenha(passwordEncoder.encode("password"));
        user.setRole(Role.ROLE_USER);
        user.setCreatedAt(OffsetDateTime.now());
        user.setEmailVerificado(true);
        userRepository.save(user);

        LoginRequestDTO request = new LoginRequestDTO("user@email.com", "password");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void login_ShouldReturnUnauthorized_WhenCredentialsAreWrong() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("user@email.com", "wrong-password");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Bad Credentials"));
    }

    @Test
    void logout_ShouldReturnNoContent_WhenSuccessfully() throws Exception {
        Usuario user = new Usuario();
        user.setNome("user");
        user.setEmail("user@email.com");
        user.setSenha(passwordEncoder.encode("password"));
        user.setRole(Role.ROLE_USER);
        user.setCreatedAt(OffsetDateTime.now());
        userRepository.save(user);
        autenticarUsuario(user);

        String refreshToken = jwtService.generateRefreshToken(user);

        RefreshToken refreshTokenEntity =
                RefreshToken.builder()
                        .token(refreshToken)
                        .usuario(user)
                        .expiraEm(jwtService.getExpirationTime(refreshToken))
                        .revogado(false)
                        .build();
        refreshTokenRepository.save(refreshTokenEntity);

        LogoutRequestDTO request = new LogoutRequestDTO(refreshToken);

        mockMvc.perform(post("/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        Optional<RefreshToken> byId = refreshTokenRepository.findById(refreshTokenEntity.getId());

        assertTrue(byId.get().isRevogado());
    }

    @Test
    void logout_ShouldReturnUnauthorized_WhenTokenNotFound() throws Exception {
        Usuario user = new Usuario();
        user.setNome("user");
        user.setEmail("user@email.com");
        user.setSenha(passwordEncoder.encode("password"));
        user.setRole(Role.ROLE_USER);
        user.setCreatedAt(OffsetDateTime.now());
        userRepository.save(user);
        autenticarUsuario(user);

        LogoutRequestDTO request = new LogoutRequestDTO("invalid-token");

        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

    }

    @Test
    void refreshToken_ShouldReturnNewAccessTokens_WhenSuccessfully() throws Exception {
        Usuario user = new Usuario();
        user.setNome("user");
        user.setEmail("user@email.com");
        user.setSenha(passwordEncoder.encode("password"));
        user.setRole(Role.ROLE_USER);
        user.setCreatedAt(OffsetDateTime.now());
        userRepository.save(user);

        String refreshToken = jwtService.generateRefreshToken(user);

        RefreshToken refreshTokenEntity =
                RefreshToken.builder()
                        .token(refreshToken)
                        .usuario(user)
                        .expiraEm(jwtService.getExpirationTime(refreshToken))
                        .revogado(false)
                        .build();
        refreshTokenRepository.save(refreshTokenEntity);

        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO(refreshToken);

        mockMvc.perform(post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());
    }

    @Test
    void refreshToken_ShouldReturnUnauthorized_WhenTokenIsInvalid() throws Exception {
        Usuario user = new Usuario();
        user.setNome("user");
        user.setEmail("user@email.com");
        user.setSenha(passwordEncoder.encode("password"));
        user.setRole(Role.ROLE_USER);
        user.setCreatedAt(OffsetDateTime.now());
        userRepository.save(user);

        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO("token");

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Token Inválido"));
    }

    @Test
    void refreshToken_ShouldReturnUnauthorized_WhenTokenIsRevogado() throws Exception {
        Usuario user = new Usuario();
        user.setNome("user");
        user.setEmail("user@email.com");
        user.setSenha(passwordEncoder.encode("password"));
        user.setRole(Role.ROLE_USER);
        user.setCreatedAt(OffsetDateTime.now());
        userRepository.save(user);

        String refreshToken = jwtService.generateRefreshToken(user);

        RefreshToken refreshTokenEntity =
                RefreshToken.builder()
                        .token(refreshToken)
                        .usuario(user)
                        .expiraEm(jwtService.getExpirationTime(refreshToken))
                        .revogado(true)
                        .build();
        refreshTokenRepository.save(refreshTokenEntity);

        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO(refreshToken);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Token revogado"));
    }

    @Test
    void refreshToken_ShouldReturnUnauthorized_WhenTokenIsExpirado() throws Exception {
        Usuario user = new Usuario();
        user.setNome("user");
        user.setEmail("user@email.com");
        user.setSenha(passwordEncoder.encode("password"));
        user.setRole(Role.ROLE_USER);
        user.setCreatedAt(OffsetDateTime.now());
        userRepository.save(user);

        String refreshToken = jwtService.generateRefreshToken(user);

        RefreshToken refreshTokenEntity =
                RefreshToken.builder()
                        .token(refreshToken)
                        .usuario(user)
                        .expiraEm(Instant.now().minus(5, ChronoUnit.DAYS))
                        .revogado(false)
                        .build();
        refreshTokenRepository.save(refreshTokenEntity);

        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO(refreshToken);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Token expirado"));
    }

    @Test
    void alterarSenha_ShouldInvalidateAllUsersRefreshTokens_WhenSuccessfully() throws Exception {
        Usuario user = new Usuario();
        user.setNome("user");
        user.setEmail("user@email.com");
        user.setSenha(passwordEncoder.encode("password"));
        user.setRole(Role.ROLE_USER);
        user.setCreatedAt(OffsetDateTime.now());
        userRepository.save(user);
        autenticarUsuario(user);

        String refreshToken = jwtService.generateRefreshToken(user);

        RefreshToken refreshTokenEntity =
                RefreshToken.builder()
                        .token(refreshToken)
                        .usuario(user)
                        .expiraEm(jwtService.getExpirationTime(refreshToken))
                        .revogado(false)
                        .build();
        refreshTokenRepository.save(refreshTokenEntity);

        AlterarSenhaRequestDTO request = new AlterarSenhaRequestDTO("password", "new-password", "new-password");

        mockMvc.perform(patch("/auth/alterar-senha")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        Optional<RefreshToken> userRefreshToken = refreshTokenRepository.findById(refreshTokenEntity.getId());

        assertTrue(userRefreshToken.get().isRevogado());
    }

    @Test
    void alterarSenha_ShouldReturnUnauthorized_WhenPasswordIsWrong() throws Exception {
        Usuario user = new Usuario();
        user.setNome("user");
        user.setEmail("user@email.com");
        user.setSenha(passwordEncoder.encode("password"));
        user.setRole(Role.ROLE_USER);
        user.setCreatedAt(OffsetDateTime.now());
        userRepository.save(user);
        autenticarUsuario(user);

        String refreshToken = jwtService.generateRefreshToken(user);

        RefreshToken refreshTokenEntity =
                RefreshToken.builder()
                        .token(refreshToken)
                        .usuario(user)
                        .expiraEm(jwtService.getExpirationTime(refreshToken))
                        .revogado(false)
                        .build();
        refreshTokenRepository.save(refreshTokenEntity);

        AlterarSenhaRequestDTO request = new AlterarSenhaRequestDTO("wrong-password", "new-password", "new-password");

        mockMvc.perform(patch("/auth/alterar-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Senha incorreta"));;

        Optional<RefreshToken> userRefreshToken = refreshTokenRepository.findById(refreshTokenEntity.getId());

        assertFalse(userRefreshToken.get().isRevogado());
    }

    @Test
    void alterarSenha_ShouldReturnUnauthorized_WhenConfirmarNovaSenhaIsWrong() throws Exception {
        Usuario user = new Usuario();
        user.setNome("user");
        user.setEmail("user@email.com");
        user.setSenha(passwordEncoder.encode("password"));
        user.setRole(Role.ROLE_USER);
        user.setCreatedAt(OffsetDateTime.now());
        userRepository.save(user);
        autenticarUsuario(user);

        String refreshToken = jwtService.generateRefreshToken(user);

        RefreshToken refreshTokenEntity =
                RefreshToken.builder()
                        .token(refreshToken)
                        .usuario(user)
                        .expiraEm(jwtService.getExpirationTime(refreshToken))
                        .revogado(false)
                        .build();
        refreshTokenRepository.save(refreshTokenEntity);

        AlterarSenhaRequestDTO request = new AlterarSenhaRequestDTO("password", "new-password", "wrong-password");

        mockMvc.perform(patch("/auth/alterar-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Confirme a nova senha corretamente"));

        Optional<RefreshToken> userRefreshToken = refreshTokenRepository.findById(refreshTokenEntity.getId());

        assertFalse(userRefreshToken.get().isRevogado());
    }

    @Test
    void resendVerificationEmail_ShouldResendVerificationEmail_WhenSuccessfully() throws Exception {
        Usuario user = new Usuario();
        user.setNome("user");
        user.setEmail("user@email.com");
        user.setSenha(passwordEncoder.encode("password"));
        user.setRole(Role.ROLE_USER);
        user.setCreatedAt(OffsetDateTime.now());
        user.setEmailVerificado(false);
        userRepository.save(user);

        ResendVerificationEmailRequestDTO request = new ResendVerificationEmailRequestDTO("user@email.com");

        mockMvc.perform(post("/auth/resend-verification-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    void forgotPassword_ShouldSendEmailToResetPassword_WhenSuccessfully() throws Exception {
        Usuario user = new Usuario();
        user.setNome("user");
        user.setEmail("user@email.com");
        user.setSenha(passwordEncoder.encode("password"));
        user.setRole(Role.ROLE_USER);
        user.setCreatedAt(OffsetDateTime.now());
        user.setEmailVerificado(true);
        userRepository.save(user);

        ForgotPasswordRequestDTO request = new ForgotPasswordRequestDTO(user.getEmail());

        mockMvc.perform(post("/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    void forgotPassword_ShouldReturnNotFound_WhenUserNotFound() throws Exception {
        ForgotPasswordRequestDTO request = new ForgotPasswordRequestDTO("user@email.com");

        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void resetPassword_ShouldResetUserPassword_WhenSuccessfully() throws Exception {
        Usuario user = new Usuario();
        user.setNome("user");
        user.setEmail("user@email.com");
        user.setSenha(passwordEncoder.encode("password"));
        user.setRole(Role.ROLE_USER);
        user.setCreatedAt(OffsetDateTime.now());
        user.setEmailVerificado(true);
        userRepository.save(user);

        String refreshToken = jwtService.generateRefreshToken(user);

        RefreshToken refreshTokenEntity =
                RefreshToken.builder()
                        .token(refreshToken)
                        .usuario(user)
                        .expiraEm(jwtService.getExpirationTime(refreshToken))
                        .revogado(false)
                        .build();
        refreshTokenRepository.save(refreshTokenEntity);

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken("reset-password-token");
        resetToken.setUsuario(user);
        resetToken.setUsado(false);
        resetToken.setExpiraEm(Instant.now().plus(1, ChronoUnit.DAYS));
        passwordResetTokenRepository.save(resetToken);

        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO("novaSenha", "novaSenha");

        mockMvc.perform(post("/auth/reset-password?token=" + resetToken.getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        Optional<Usuario> usuarioNovaSenha = userRepository.findByEmail(user.getEmail());
        Optional<PasswordResetToken> resetPasswordToken = passwordResetTokenRepository.findByToken("reset-password-token");
        Optional<RefreshToken> userRefreshToken = refreshTokenRepository.findByToken(refreshTokenEntity.getToken());

        boolean matches = passwordEncoder.matches(request.novaSenha(), usuarioNovaSenha.get().getSenha());

        assertTrue(userRefreshToken.get().isRevogado());
        assertTrue(resetPasswordToken.get().isUsado());
        assertTrue(matches);
    }

    @Test
    void resetPassword_ShouldUnauthorized_WhenUserNotFound() throws Exception {
        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO("novaSenha", "novaSenha");

        mockMvc.perform(post("/auth/reset-password?token=token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void resetPassword_ShouldUnauthorized_WhenTokenIsInvalid() throws Exception {
        Usuario user = new Usuario();
        user.setNome("user");
        user.setEmail("user@email.com");
        user.setSenha(passwordEncoder.encode("password"));
        user.setRole(Role.ROLE_USER);
        user.setCreatedAt(OffsetDateTime.now());
        user.setEmailVerificado(true);
        userRepository.save(user);

        String refreshToken = jwtService.generateRefreshToken(user);

        RefreshToken refreshTokenEntity =
                RefreshToken.builder()
                        .token(refreshToken)
                        .usuario(user)
                        .expiraEm(jwtService.getExpirationTime(refreshToken))
                        .revogado(false)
                        .build();
        refreshTokenRepository.save(refreshTokenEntity);

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken("reset-password-token");
        resetToken.setUsuario(user);
        resetToken.setUsado(true);
        resetToken.setExpiraEm(Instant.now().minus(1, ChronoUnit.DAYS));
        passwordResetTokenRepository.save(resetToken);

        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO("novaSenha", "novaSenha");

        mockMvc.perform(post("/auth/reset-password?token=" + resetToken.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        Optional<Usuario> usuarioNovaSenha = userRepository.findByEmail(user.getEmail());
        Optional<RefreshToken> userRefreshToken = refreshTokenRepository.findByToken(refreshTokenEntity.getToken());

        boolean matches = passwordEncoder.matches(request.novaSenha(), usuarioNovaSenha.get().getSenha());

        assertFalse(userRefreshToken.get().isRevogado());
        assertFalse(matches);
    }

    protected void autenticarUsuario(Usuario usuario) {

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        usuario,
                        null,
                        usuario.getAuthorities()
                );

        SecurityContext context =
                SecurityContextHolder.createEmptyContext();

        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);
    }
}
