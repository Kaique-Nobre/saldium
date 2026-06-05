package com.saldium.saldium.security.auth;

import com.saldium.saldium.exceptions.auth.BadCredentialsException;
import com.saldium.saldium.exceptions.auth.EmailJaRegistradoException;
import com.saldium.saldium.exceptions.auth.TokenInvalidoException;
import com.saldium.saldium.exceptions.auth.UsuarioNaoEncontradoException;
import com.saldium.saldium.security.auth.dto.*;
import com.saldium.saldium.security.jwt.JwtService;
import com.saldium.saldium.security.user.Role;
import com.saldium.saldium.security.user.UserRepository;
import com.saldium.saldium.security.user.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public void cadastrar(CadastroDTO request) {
        if(userRepository.existsByEmail(request.email())) {
            throw new EmailJaRegistradoException("O Email " +request.email()+ " já está cadastrado, tente um Email diferente");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        usuario.setSenha(passwordEncoder.encode(request.senha()));
        usuario.setRole(Role.ROLE_USER);
        usuario.setCreatedAt(OffsetDateTime.now());

        userRepository.save(usuario);
    }

    public LoginResponseDTO login(LoginRequestDTO request) {
        try {
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.senha()));

            Usuario usuario = (Usuario) authentication.getPrincipal();

            String accessToken = jwtService.generateAccessToken(usuario);
            String refreshToken = jwtService.generateRefreshToken(usuario);

            return new LoginResponseDTO(accessToken, refreshToken);
        }catch (AuthenticationException e) {
            throw new BadCredentialsException("Email ou senha estão incorretos");
        }
    }

    public RefreshTokenResponseDTO refreshToken(RefreshTokenRequestDTO request) {
        String email = jwtService.validateRefreshToken(request.refreshToken());

        Usuario usuario  = userRepository.findByEmail(email)
                .orElseThrow(() -> new TokenInvalidoException("Token invalido ou expirado"));

        String accessToken = jwtService.generateAccessToken(usuario);

        return new RefreshTokenResponseDTO(accessToken);
    }
}
