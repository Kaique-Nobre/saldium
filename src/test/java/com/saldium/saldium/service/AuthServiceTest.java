package com.saldium.saldium.service;

import com.saldium.saldium.exceptions.auth.BadCredentialsException;
import com.saldium.saldium.exceptions.auth.EmailJaRegistradoException;
import com.saldium.saldium.exceptions.auth.TokenInvalidoException;
import com.saldium.saldium.security.auth.AuthService;
import com.saldium.saldium.security.auth.dto.*;
import com.saldium.saldium.security.jwt.JwtService;
import com.saldium.saldium.security.token.RefreshToken;
import com.saldium.saldium.security.token.RefreshTokenRepository;
import com.saldium.saldium.security.token.RefreshTokenRequestDTO;
import com.saldium.saldium.security.token.RefreshTokenResponseDTO;
import com.saldium.saldium.security.user.UserRepository;
import com.saldium.saldium.security.user.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static com.saldium.saldium.util.auth.CadastroCreator.criarCadastroDTO;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void cadastrar_ShouldSaveUser_WhenSuccessfully() throws Exception {
        CadastroDTO request = criarCadastroDTO();

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.senha())).thenReturn("senha-hasheada");

        authService.cadastrar(request);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);

        verify(userRepository).save(captor.capture());

        Usuario usuario = captor.getValue();

        assertEquals(request.nome(), usuario.getNome());
        assertEquals(request.email(), usuario.getEmail());
        assertEquals("senha-hasheada", usuario.getSenha());
    }

    @Test
    void cadastrar_ShouldThrowException_WhenEmailAlreadyExists() throws Exception {
        CadastroDTO request = criarCadastroDTO();

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThrows(EmailJaRegistradoException.class, () -> authService.cadastrar(request));

        verify(userRepository, never()).save(any(Usuario.class));
    }

    @Test
    void login_ShouldReturnTokens_WhenSuccessfully() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("user@email.com", "senha");

        Usuario usuario = new Usuario();
        usuario.setEmail(request.email());

        Authentication authentication = mock(Authentication.class);

        when(authenticationManager
                .authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(authentication.getPrincipal()).thenReturn(usuario);

        when(jwtService.generateAccessToken(any(Usuario.class))).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(Usuario.class))).thenReturn("refresh-token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(new RefreshToken());

        LoginResponseDTO tokens = authService.login(request);

        assertNotNull(tokens);
        assertEquals("access-token", tokens.accessToken());
        assertEquals("refresh-token", tokens.refreshToken());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateAccessToken(any(Usuario.class));
        verify(jwtService).generateRefreshToken(any(Usuario.class));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void login_ShouldThrowBadCredentials_WhenCredentialsAreIncorrect() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("wrong@email.com", "senha-errada");

        when(authenticationManager
                .authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(BadCredentialsException.class);

        assertThrows(BadCredentialsException.class, () -> authService.login(request));

        verify(jwtService, never()).generateAccessToken(any(Usuario.class));
        verify(jwtService, never()).generateRefreshToken(any(Usuario.class));
    }

    @Test
    void refreshToken_ShouldReturnNewAccessToken_WhenSuccessfully() throws Exception {
        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO("refresh-token");

        Usuario usuario = new Usuario();
        usuario.setEmail("user@email.com");
        RefreshToken refreshTokenEntity =
                RefreshToken.builder()
                        .token(request.refreshToken())
                        .usuario(usuario)
                        .expiraEm(Instant.now().plus(5, ChronoUnit.DAYS))
                        .revogado(false)
                        .build();

        when(jwtService.validateRefreshToken(request.refreshToken())).thenReturn(usuario.getEmail());
        when(refreshTokenRepository.findByToken(request.refreshToken())).thenReturn(Optional.of(refreshTokenEntity));
        when(userRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(jwtService.generateAccessToken(any(Usuario.class))).thenReturn("access-token");

        RefreshTokenResponseDTO response = authService.refreshToken(request);

        assertNotNull(response);
        assertEquals("access-token", response.accessToken());

        verify(jwtService).validateRefreshToken(request.refreshToken());
        verify(jwtService).generateAccessToken(any(Usuario.class));
        verify(userRepository).findByEmail(usuario.getEmail());
    }

    @Test
    void refreshToken_ShouldThrowException_WhenTokenIsExpirado() throws Exception {
        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO("refresh-token");

        Usuario usuario = new Usuario();
        usuario.setEmail("user@email.com");
        RefreshToken refreshTokenEntity =
                RefreshToken.builder()
                        .token(request.refreshToken())
                        .usuario(usuario)
                        .expiraEm(Instant.now().plus(5, ChronoUnit.DAYS))
                        .revogado(false)
                        .build();

        when(jwtService.validateRefreshToken(request.refreshToken())).thenReturn(usuario.getEmail());
        when(refreshTokenRepository.findByToken(request.refreshToken())).thenReturn(Optional.of(refreshTokenEntity));

       assertThrows(TokenInvalidoException.class, () -> authService.refreshToken(request));

        verify(jwtService).validateRefreshToken(request.refreshToken());
        verify(jwtService, never()).generateAccessToken(any(Usuario.class));
    }

    @Test
    void refreshToken_ShouldThrowException_WhenTokenIsRevogado() throws Exception {
        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO("refresh-token");

        Usuario usuario = new Usuario();
        usuario.setEmail("user@email.com");
        RefreshToken refreshTokenEntity =
                RefreshToken.builder()
                        .token(request.refreshToken())
                        .usuario(usuario)
                        .expiraEm(Instant.now())
                        .revogado(true)
                        .build();

        when(jwtService.validateRefreshToken(request.refreshToken())).thenReturn(usuario.getEmail());
        when(refreshTokenRepository.findByToken(request.refreshToken())).thenReturn(Optional.of(refreshTokenEntity));

        assertThrows(TokenInvalidoException.class, () -> authService.refreshToken(request));

        verify(jwtService).validateRefreshToken(request.refreshToken());
        verify(jwtService, never()).generateAccessToken(any(Usuario.class));
    }

    @Test
    void refreshToken_ShouldThrowInvalidToken_WhenRefreshTokenIsInvalid() throws Exception {
        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO("refresh-token");

        when(jwtService.validateRefreshToken(request.refreshToken())).thenThrow(TokenInvalidoException.class);

        assertThrows(TokenInvalidoException.class, () -> authService.refreshToken(request));

        verify(jwtService, never()).generateAccessToken(any(Usuario.class));
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void logout_ShouldRevokeUserRefreshToken_WhenSuccessfully() throws Exception {
        LogoutRequestDTO request = new LogoutRequestDTO("refresh-token");

        Usuario usuario = new Usuario();
        usuario.setEmail("user@email.com");
        RefreshToken refreshToken =
                RefreshToken.builder()
                        .token(request.refreshToken())
                        .usuario(usuario)
                        .expiraEm(Instant.now().plus(5, ChronoUnit.DAYS))
                        .revogado(true)
                        .build();

        when(refreshTokenRepository.findByToken(request.refreshToken())).thenReturn(Optional.of(refreshToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

        authService.logout(request);

        ArgumentCaptor<RefreshToken> captor =
                ArgumentCaptor.forClass(RefreshToken.class);

        verify(refreshTokenRepository).findByToken("refresh-token");
        verify(refreshTokenRepository).save(captor.capture());

        RefreshToken tokenSalvo = captor.getValue();

        assertEquals(tokenSalvo.getToken(), request.refreshToken());
        assertTrue(tokenSalvo.isRevogado());
    }

    @Test
    void logout_ShouldThrowException_WhenTokenNotFound() {
        LogoutRequestDTO request =
                new LogoutRequestDTO("invalid-token");

        when(refreshTokenRepository.findByToken("invalid-token"))
                .thenReturn(Optional.empty());

        assertThrows(
                TokenInvalidoException.class,
                () -> authService.logout(request)
        );

        verify(refreshTokenRepository).findByToken("invalid-token");

        verify(refreshTokenRepository, never())
                .save(any());
    }

    @Test
    void alterarSenha_ShouldAlterarSenha_WhenSuccessfully() throws Exception {
        AlterarSenhaRequestDTO request =
                new AlterarSenhaRequestDTO( "senha123", "nova-senha", "nova-senha");

        Usuario usuario = new Usuario();
        usuario.setEmail("user@email.com");
        usuario.setSenha("senha123");

        mockAuthenticatedUser(usuario);

        RefreshToken token = RefreshToken.builder()
                .token("refresh-token")
                .usuario(usuario)
                .expiraEm(Instant.now().plus(5, ChronoUnit.DAYS))
                .revogado(true)
                .build();

        List<RefreshToken> refreshTokens = List.of(token);

        when(passwordEncoder.matches(usuario.getSenha(), request.senhaAtual())).thenReturn(true);
        when(userRepository.findById(any())).thenReturn(Optional.of(usuario));
        when(refreshTokenRepository.findAllByUsuario(usuario)).thenReturn(refreshTokens);
        when(refreshTokenRepository.saveAll(refreshTokens)).thenReturn(refreshTokens);

        authService.alterarSenha(request);

        verify(refreshTokenRepository).findAllByUsuario(usuario);
        verify(refreshTokenRepository).saveAll(refreshTokens);
    }

    @Test
    void alterarSenha_ShouldThrowException_WhenSenhaIsIncorret() throws Exception {
        AlterarSenhaRequestDTO request =
                new AlterarSenhaRequestDTO( "12345", "nova-senha", "nova-senha");

        Usuario usuario = new Usuario();
        usuario.setEmail("user@email.com");
        usuario.setSenha("senha123");

        mockAuthenticatedUser(usuario);

        when(passwordEncoder.matches(request.senhaAtual(), usuario.getSenha())).thenThrow(BadCredentialsException.class);

        assertThrows(BadCredentialsException.class, () -> authService.alterarSenha(request));

        verify(passwordEncoder).matches(request.senhaAtual(), usuario.getSenha());
        verify(refreshTokenRepository, never()).saveAll(anyList());
    }

    @Test
    void alterarSenha_ShouldThrowException_WhenNovaSenhaÉIgualSenhaAntiga() throws Exception {
        AlterarSenhaRequestDTO request =
                new AlterarSenhaRequestDTO( "senha123", "senha123", "senha123");

        Usuario usuario = new Usuario();
        usuario.setEmail("user@email.com");
        usuario.setSenha("senha123");

        mockAuthenticatedUser(usuario);

        when(passwordEncoder.matches(request.novaSenha(), request.senhaAtual())).thenThrow(BadCredentialsException.class);

        assertThrows(BadCredentialsException.class, () -> authService.alterarSenha(request));

        verify(passwordEncoder).matches(request.senhaAtual(), usuario.getSenha());
        verify(refreshTokenRepository, never()).saveAll(anyList());
    }

    private static void mockAuthenticatedUser(Usuario usuario) {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(usuario);

        SecurityContextHolder.setContext(securityContext);
    }
}
