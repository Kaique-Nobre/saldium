package com.saldium.saldium.security.auth;

import com.saldium.saldium.exceptions.BadRequestException;
import com.saldium.saldium.exceptions.auth.BadCredentialsException;
import com.saldium.saldium.exceptions.auth.EmailJaRegistradoException;
import com.saldium.saldium.exceptions.auth.TokenInvalidoException;
import com.saldium.saldium.exceptions.auth.UsuarioNaoEncontradoException;
import com.saldium.saldium.security.config.AppProperties;
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
import com.saldium.saldium.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordResetTokenService passwordResetTokenService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final VerificationTokenService verificationTokenService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final AppProperties appProperties;

    @Transactional(rollbackFor = Exception.class)
    public void cadastrar(CadastroDTO request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailJaRegistradoException("O Email " + request.email() + " já está cadastrado, tente um Email diferente");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        usuario.setSenha(passwordEncoder.encode(request.senha()));
        usuario.setRole(Role.ROLE_USER);
        usuario.setCreatedAt(OffsetDateTime.now());
        usuario.setEmailVerificado(false);

        Usuario usuarioSalvo = userRepository.save(usuario);

        VerificationToken verificationToken = verificationTokenService.createVerificationToken(usuarioSalvo);

        sendVerificationEmail(verificationToken, usuarioSalvo);
    }

    @Transactional(rollbackFor = Exception.class)
    public LoginResponseDTO login(LoginRequestDTO request) {
        try {
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.senha()));

            Usuario usuario = (Usuario) authentication.getPrincipal();

            if (!usuario.isEmailVerificado()) {
                throw new BadCredentialsException("Email não verificado");
            }

            String accessToken = jwtService.generateAccessToken(usuario);
            String refreshToken = jwtService.generateRefreshToken(usuario);

            RefreshToken refreshTokenEntity =
                    RefreshToken.builder()
                            .token(refreshToken)
                            .usuario(usuario)
                            .expiraEm(jwtService.getExpirationTime(refreshToken))
                            .revogado(false)
                            .build();

            refreshTokenRepository.save(refreshTokenEntity);

            return new LoginResponseDTO(accessToken, refreshToken);
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Email ou senha estão incorretos");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void logout(LogoutRequestDTO request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new TokenInvalidoException("Token invalido ou expirado"));

        refreshToken.setRevogado(true);

        refreshTokenRepository.save(refreshToken);
    }

    @Transactional(rollbackFor = Exception.class)
    public RefreshTokenResponseDTO refreshToken(RefreshTokenRequestDTO request) {
        String email = jwtService.validateRefreshToken(request.refreshToken());

        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new TokenInvalidoException("Token invalido ou expirado"));

        if (refreshToken.isRevogado()) {
            throw new TokenInvalidoException("Token revogado");
        }

        if (refreshToken.getExpiraEm().isBefore(Instant.now())) {
            throw new TokenInvalidoException("Token expirado");
        }

        Usuario usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new TokenInvalidoException("Token invalido ou expirado"));

        String accessToken = jwtService.generateAccessToken(usuario);

        return new RefreshTokenResponseDTO(accessToken);
    }

    @Transactional(rollbackFor = Exception.class)
    public void alterarSenha(AlterarSenhaRequestDTO request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Usuario usuarioAutenticado = (Usuario) authentication.getPrincipal();

        if (!passwordEncoder.matches(request.senhaAtual(), usuarioAutenticado.getSenha())) {
            throw new BadCredentialsException("Senha incorreta");
        }

        if (!request.novaSenha().equals(request.confirmarNovaSenha())) {
            throw new BadCredentialsException("Confirme a nova senha corretamente");
        }

        if (passwordEncoder.matches(request.novaSenha(), usuarioAutenticado.getSenha())) {
            throw new BadCredentialsException("Nova senha não pode ser igual a senha antiga");
        }

        Usuario usuario = userRepository.findById(usuarioAutenticado.getId())
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuario inexistente"));

        usuario.setSenha(passwordEncoder.encode(request.novaSenha()));

        List<RefreshToken> tokens = refreshTokenRepository.findAllByUsuario(usuario);

        tokens.forEach(token -> token.setRevogado(true));

        refreshTokenRepository.saveAll(tokens);
        userRepository.save(usuario);
    }

    @Transactional(rollbackFor = Exception.class)
    public void resendVerificationEmail(String email) {
        Usuario usuario = userRepository.findByEmail(email)
                        .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuario inexistente"));

        if (usuario.isEmailVerificado()) {
            throw new BadRequestException("Email já validado");
        }

        verificationTokenRepository.deleteAllByUsuario(usuario);

        VerificationToken newToken = verificationTokenService.createVerificationToken(usuario);

        sendVerificationEmail(newToken,  usuario);
    }

    @Transactional(rollbackFor = Exception.class)
    public void forgotPassword(ForgotPasswordRequestDTO request) {
        Usuario usuario = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuario inexistente"));

        List<PasswordResetToken> tokens = passwordResetTokenRepository.findAllByUsuario(usuario);

        tokens.forEach(token -> token.setUsado(true));
        passwordResetTokenRepository.saveAll(tokens);

        PasswordResetToken passwordResetToken = passwordResetTokenService.createPasswordResetToken(usuario);

        String resetUrl =
                appProperties.frontendUrl()
                +"/reset-password?token="
                        + passwordResetToken.getToken();

        emailService.sendEmail(
                usuario.getEmail(),
                "Recuperação de senha",
                """
                Você solicitou a redefinição da sua senha.
        
                Clique no link abaixo:
        
                %s
       
                """.formatted(resetUrl)
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(String token, ResetPasswordRequestDTO request) {
        PasswordResetToken passwordResetToken =
                passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenInvalidoException("Token invalido ou expirado"));

        if(passwordResetToken.isUsado()) {
            throw new TokenInvalidoException("Token já utilizado");
        }
        if(passwordResetToken.getExpiraEm().isBefore(Instant.now())) {
            throw new TokenInvalidoException("Token expirado");
        }
        if (!request.novaSenha().equals(request.confirmarNovaSenha())) {
            throw new BadCredentialsException("Confirme a nova senha corretamente");
        }

        Usuario usuario = passwordResetToken.getUsuario();

        if (passwordEncoder.matches(request.novaSenha(), usuario.getSenha())) {
            throw new BadCredentialsException("A nova senha não pode ser igual à senha atual");
        }

        usuario.setSenha(passwordEncoder.encode(request.novaSenha()));

        List<RefreshToken> refreshTokens = refreshTokenRepository.findAllByUsuario(usuario);

        refreshTokens.forEach(t -> t.setRevogado(true));

        refreshTokenRepository.saveAll(refreshTokens);

        passwordResetToken.setUsado(true);
        passwordResetTokenRepository.save(passwordResetToken);

        userRepository.save(usuario);
    }

    private void sendVerificationEmail(VerificationToken verificationToken, Usuario usuarioSalvo) {
        String verificationUrl =
                appProperties.backendUrl()
                + "/auth/verify-email?token="
                        + verificationToken.getToken();

        emailService.sendEmail(
                usuarioSalvo.getEmail(),
                "Verificação de Email",
                """
                Bem-vindo ao Saldium!
        
                Clique no link abaixo para confirmar seu email:
        
                %s
                """.formatted(verificationUrl)
        );
    }
}
