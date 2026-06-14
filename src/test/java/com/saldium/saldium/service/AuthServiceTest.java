package com.saldium.saldium.service;

import com.saldium.saldium.exceptions.BadRequestException;
import com.saldium.saldium.exceptions.auth.BadCredentialsException;
import com.saldium.saldium.exceptions.auth.EmailJaRegistradoException;
import com.saldium.saldium.exceptions.auth.TokenInvalidoException;
import com.saldium.saldium.security.auth.AuthService;
import com.saldium.saldium.security.auth.dto.*;
import com.saldium.saldium.security.jwt.JwtService;
import com.saldium.saldium.security.passwordResetToken.*;
import com.saldium.saldium.security.refreshToken.RefreshToken;
import com.saldium.saldium.security.refreshToken.RefreshTokenRepository;
import com.saldium.saldium.security.refreshToken.RefreshTokenRequestDTO;
import com.saldium.saldium.security.refreshToken.RefreshTokenResponseDTO;
import com.saldium.saldium.security.user.Role;
import com.saldium.saldium.security.user.UserRepository;
import com.saldium.saldium.security.user.Usuario;
import com.saldium.saldium.security.verificationToken.VerificationToken;
import com.saldium.saldium.security.verificationToken.VerificationTokenRepository;
import com.saldium.saldium.security.verificationToken.VerificationTokenService;
import lombok.extern.slf4j.Slf4j;
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
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static com.saldium.saldium.util.auth.CadastroCreator.criarCadastroDTO;
import static com.saldium.saldium.util.usuario.UsuarioCreator.criarUsuario;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private VerificationTokenService verificationTokenService;

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @Mock
    private PasswordResetTokenService passwordResetTokenService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    @Test
    void cadastrar_ShouldSaveUser_WhenSuccessfully() throws Exception {
        CadastroDTO request = criarCadastroDTO();

        Usuario usuario = criarUsuario();

        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken("token");

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.senha())).thenReturn("senha-hasheada");
        when(userRepository.save(any(Usuario.class))).thenReturn(usuario);

        when(verificationTokenService.createVerificationToken(any(Usuario.class))).thenReturn(verificationToken);

        authService.cadastrar(request);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);

        verify(userRepository).save(captor.capture());
        verify(verificationTokenService)
                .createVerificationToken(usuario);

        verify(emailService).sendEmail(
                eq("user@email.com"),
                eq("Verificação de Email"),
                contains("token"));

        Usuario usuarioSalvo = captor.getValue();

        assertEquals(request.nome(), usuarioSalvo.getNome());
        assertEquals(request.email(), usuarioSalvo.getEmail());
        assertEquals("senha-hasheada", usuarioSalvo.getSenha());
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
        usuario.setEmailVerificado(true);

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
    void login_ShouldUnauthorized_WhenUsersEmailIsNotVerified() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("user@email.com", "senha");

        Usuario usuario = new Usuario();
        usuario.setEmail(request.email());
        usuario.setEmailVerificado(false);

        Authentication authentication = mock(Authentication.class);

        when(authenticationManager
                .authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(authentication.getPrincipal()).thenReturn(usuario);

        assertThrows(BadCredentialsException.class, () -> authService.login(request));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, never()).generateAccessToken(any(Usuario.class));
        verify(jwtService, never()).generateRefreshToken(any(Usuario.class));
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
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

    @Test
    void resendVerificationEmail_ShouldResendVerificationEmail_WhenSuccessfully() throws Exception {
        Usuario usuario = criarUsuario();
        usuario.setEmailVerificado(false);

        VerificationToken newToken = new VerificationToken();
        newToken.setToken("new-verification-token");

        when(userRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        doNothing().when(verificationTokenRepository).deleteAllByUsuario(usuario);
        when(verificationTokenService.createVerificationToken(usuario)).thenReturn(newToken);

        authService.resendVerificationEmail(usuario.getEmail());

        verify(verificationTokenRepository).deleteAllByUsuario(usuario);
        verify(verificationTokenService).createVerificationToken(usuario);
        verify(emailService).sendEmail(
                eq("user@email.com"),
                eq("Verificação de Email"),
                contains("new-verification-token"));
    }

    @Test
    void resendVerificationEmail_ShouldReturnBadRequest_WhenUsersEmailIsAlreadyVerified() throws Exception {
        Usuario usuario = criarUsuario();
        usuario.setEmailVerificado(true);

        when(userRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));

        assertThrows(BadRequestException.class, () -> authService.resendVerificationEmail(usuario.getEmail()));

        verify(verificationTokenRepository, never()).deleteAllByUsuario(usuario);
        verify(verificationTokenService, never()).createVerificationToken(usuario);
    }

    @Test
    void forgotPassword_ShouldSendResetPasswordEmail_WhenSuccessfully() {
        ForgotPasswordRequestDTO request = new ForgotPasswordRequestDTO("user@email.com");

        Usuario usuario = criarUsuario();

        PasswordResetToken token = new PasswordResetToken();
        token.setToken("reset-password");
        token.setUsado(true);

        PasswordResetToken newToken = new PasswordResetToken();
        token.setToken("reset-password-token");
        token.setUsado(false);

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(usuario));
        when(passwordResetTokenRepository.findAllByUsuario(usuario)).thenReturn(List.of(token));
        when(passwordResetTokenRepository.saveAll(List.of(token))).thenReturn(List.of(token));
        when(passwordResetTokenService.createPasswordResetToken(any(Usuario.class))).thenReturn(newToken);

        authService.forgotPassword(request);

        verify(passwordResetTokenRepository).findAllByUsuario(any(Usuario.class));
        verify(passwordResetTokenRepository).saveAll(anyList());
        verify(passwordResetTokenService).createPasswordResetToken(any(Usuario.class));
        verify(emailService).sendEmail(
                eq("user@email.com"),
                eq("Recuperação de senha"),
                contains("Você solicitou a redefinição da sua senha."));

    }

    @Test
    void resetPassword_ShouldResetPassword_WhenSuccessfully() {
        Usuario usuario = criarUsuario();
        usuario.setSenha("senha123");

        String token = "reset-password-token";

        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO("novaSenha", "novaSenha");

        PasswordResetToken passwordResetToken = new PasswordResetToken();

        passwordResetToken.setToken(token);
        passwordResetToken.setUsuario(usuario);
        passwordResetToken.setUsado(false);
        passwordResetToken.setExpiraEm(Instant.now().plus(1, ChronoUnit.DAYS));

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setRevogado(false);

        when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.of(passwordResetToken));

        when(refreshTokenRepository.findAllByUsuario(usuario)).thenReturn(List.of(refreshToken));

        when(passwordEncoder.matches(
                request.novaSenha(),
                usuario.getSenha()))
                .thenReturn(false);

        when(passwordEncoder.encode(request.novaSenha())).thenReturn("senhaCriptografada");

        authService.resetPassword(token, request);

        assertEquals("senhaCriptografada", usuario.getSenha());

        assertTrue(refreshToken.isRevogado());

        assertTrue(passwordResetToken.isUsado());

        verify(refreshTokenRepository).saveAll(anyList());

        verify(passwordResetTokenRepository).save(passwordResetToken);

        verify(userRepository).save(usuario);
    }

    @Test
    void resetPassword_ShouldReturnUnauthorized_WhenTokenIsInvalido() {
        Usuario usuario = criarUsuario();
        usuario.setSenha("senha123");

        String token = "reset-password-token";

        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO("novaSenha", "novaSenha");

        PasswordResetToken passwordResetToken = new PasswordResetToken();

        passwordResetToken.setToken(token);
        passwordResetToken.setUsuario(usuario);
        passwordResetToken.setUsado(true);
        passwordResetToken.setExpiraEm(Instant.now().minus(1, ChronoUnit.DAYS));

        when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.of(passwordResetToken));

        assertThrows(TokenInvalidoException.class, () -> authService.resetPassword(token, request));

        verify(refreshTokenRepository, never()).saveAll(anyList());

        verify(passwordResetTokenRepository, never()).save(passwordResetToken);

        verify(userRepository, never()).save(usuario);
    }

    @Test
    void resetPassword_ShouldUnauthorized_WhenPasswordDontMatch() {
        Usuario usuario = criarUsuario();
        usuario.setSenha("senha123");

        String token = "reset-password-token";

        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO("novaSenha", "novaSenha");

        PasswordResetToken passwordResetToken = new PasswordResetToken();

        passwordResetToken.setToken(token);
        passwordResetToken.setUsuario(usuario);
        passwordResetToken.setUsado(false);
        passwordResetToken.setExpiraEm(Instant.now().plus(1, ChronoUnit.DAYS));

        when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.of(passwordResetToken));

        when(passwordEncoder.matches(
                request.novaSenha(),
                usuario.getSenha()))
                .thenReturn(true);

        assertThrows(BadCredentialsException.class, () -> authService.resetPassword(token, request));

        verify(refreshTokenRepository, never()).saveAll(anyList());

        verify(passwordResetTokenRepository, never()).save(passwordResetToken);

        verify(userRepository, never()).save(usuario);
    }

    private static void mockAuthenticatedUser(Usuario usuario) {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(usuario);

        SecurityContextHolder.setContext(securityContext);
    }
}
