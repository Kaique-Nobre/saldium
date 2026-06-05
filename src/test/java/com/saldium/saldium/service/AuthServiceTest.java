package com.saldium.saldium.service;

import com.saldium.saldium.exceptions.auth.BadCredentialsException;
import com.saldium.saldium.exceptions.auth.EmailJaRegistradoException;
import com.saldium.saldium.exceptions.auth.TokenInvalidoException;
import com.saldium.saldium.security.auth.AuthService;
import com.saldium.saldium.security.auth.dto.*;
import com.saldium.saldium.security.jwt.JwtService;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static com.saldium.saldium.util.auth.CadastroCreator.criarCadastroDTO;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

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

        LoginResponseDTO tokens = authService.login(request);

        assertNotNull(tokens);
        assertEquals("access-token", tokens.accessToken());
        assertEquals("refresh-token", tokens.refreshToken());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateAccessToken(any(Usuario.class));
        verify(jwtService).generateRefreshToken(any(Usuario.class));
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

        when(jwtService.validateRefreshToken(request.refreshToken())).thenReturn(usuario.getEmail());
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
    void refreshToken_ShouldThrowInvalidToken_WhenRefreshTokenIsInvalid() throws Exception {
        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO("refresh-token");

        when(jwtService.validateRefreshToken(request.refreshToken())).thenThrow(TokenInvalidoException.class);

        assertThrows(TokenInvalidoException.class, () -> authService.refreshToken(request));

        verify(jwtService, never()).generateAccessToken(any(Usuario.class));
        verify(userRepository, never()).findByEmail(anyString());
    }
}
