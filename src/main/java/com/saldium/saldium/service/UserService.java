package com.saldium.saldium.service;

import com.saldium.saldium.dto.user.UserResponseDTO;
import com.saldium.saldium.exceptions.BadRequestException;
import com.saldium.saldium.repository.CategoriaRepository;
import com.saldium.saldium.repository.TransacaoRepository;
import com.saldium.saldium.security.auth.dto.DeletarContaRequestDTO;
import com.saldium.saldium.security.passwordResetToken.PasswordResetTokenRepository;
import com.saldium.saldium.security.refreshToken.RefreshTokenRepository;
import com.saldium.saldium.security.user.UserRepository;
import com.saldium.saldium.security.user.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final CategoriaRepository categoriaRepository;
    private final TransacaoRepository transacaoRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Transactional(rollbackFor = Exception.class)
    public void deletarUsuario(DeletarContaRequestDTO request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Usuario usuarioAutenticado = (Usuario) authentication.getPrincipal();

        if (!passwordEncoder.matches(request.senha(), usuarioAutenticado.getSenha())) {
            throw new BadRequestException("Senha incorreta");
        }

        transacaoRepository.deleteAllFromUsuario(usuarioAutenticado.getId());
        categoriaRepository.deleteAllFromUsuario(usuarioAutenticado.getId());
        passwordResetTokenRepository.deleteAllFromUsuario(usuarioAutenticado.getId());
        refreshTokenRepository.deleteAllFromUsuario(usuarioAutenticado.getId());
        userRepository.deleteById(usuarioAutenticado.getId());
    }

    public UserResponseDTO getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuarioAutenticado = (Usuario) authentication.getPrincipal();

        return new UserResponseDTO(
                usuarioAutenticado.getNome(),
                usuarioAutenticado.getEmail(),
                usuarioAutenticado.getCreatedAt().toLocalDate()
        );
    }
}
