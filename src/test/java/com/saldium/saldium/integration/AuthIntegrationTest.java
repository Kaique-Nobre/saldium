package com.saldium.saldium.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saldium.saldium.security.auth.dto.AlterarSenhaRequestDTO;
import com.saldium.saldium.security.auth.dto.CadastroDTO;
import com.saldium.saldium.security.auth.dto.LoginRequestDTO;
import com.saldium.saldium.security.auth.dto.LogoutRequestDTO;
import com.saldium.saldium.security.jwt.JwtService;
import com.saldium.saldium.security.token.RefreshToken;
import com.saldium.saldium.security.token.RefreshTokenRepository;
import com.saldium.saldium.security.token.RefreshTokenRequestDTO;
import com.saldium.saldium.security.user.Role;
import com.saldium.saldium.security.user.UserRepository;
import com.saldium.saldium.security.user.Usuario;
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
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanDatabase() {
        refreshTokenRepository.deleteAll();
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
    void cadastra_ShouldReturnConflict_WhenEmailAlreadyRegistered() throws Exception {
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
