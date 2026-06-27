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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.OffsetDateTime;

import static com.saldium.saldium.util.usuario.UsuarioCreator.criarUsuario;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TransacaoRepository transacaoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void deletarUsuario_ShouldDeleteUsuario_WhenSuccessfully() throws Exception {
        DeletarContaRequestDTO request = new DeletarContaRequestDTO("senhaUsuario");

        Usuario usuario = criarUsuario();
        usuario.setSenha(passwordEncoder.encode("senhaUsuario"));
        mockAuthenticatedUser(usuario);

        when(passwordEncoder.matches(request.senha(), usuario.getSenha())).thenReturn(true);
        doNothing().when(transacaoRepository).deleteAllFromUsuario(usuario.getId());
        doNothing().when(categoriaRepository).deleteAllFromUsuario(usuario.getId());
        doNothing().when(refreshTokenRepository).deleteAllFromUsuario(usuario.getId());
        doNothing().when(passwordResetTokenRepository).deleteAllFromUsuario(usuario.getId());
        doNothing().when(userRepository).deleteById(usuario.getId());

        userService.deletarUsuario(request);

        verify(transacaoRepository).deleteAllFromUsuario(usuario.getId());
        verify(categoriaRepository).deleteAllFromUsuario(usuario.getId());
        verify(refreshTokenRepository).deleteAllFromUsuario(usuario.getId());
        verify(passwordResetTokenRepository).deleteAllFromUsuario(usuario.getId());
        verify(userRepository).deleteById(usuario.getId());
    }

    @Test
    void deletarUsuario_ShouldThrowBadRquest_WhenPasswordIsWrong() throws Exception {
        DeletarContaRequestDTO request = new DeletarContaRequestDTO("senhaErrada");

        Usuario usuario = criarUsuario();
        usuario.setSenha(passwordEncoder.encode("senhaUsuario"));
        mockAuthenticatedUser(usuario);

        when(passwordEncoder.matches(request.senha(), usuario.getSenha())).thenReturn(false);

        assertThrows(BadRequestException.class, () -> userService.deletarUsuario(request));

        verify(transacaoRepository, never()).deleteAllFromUsuario(usuario.getId());
        verify(categoriaRepository, never()).deleteAllFromUsuario(usuario.getId());
        verify(refreshTokenRepository, never()).deleteAllFromUsuario(usuario.getId());
        verify(passwordResetTokenRepository, never()).deleteAllFromUsuario(usuario.getId());
        verify(userRepository, never()).deleteById(usuario.getId());
    }

    @Test
    void getUserInfo_ShouldReturnUserInfo_WhenSuccessfully() throws Exception {
        Usuario usuario = criarUsuario();
        usuario.setCreatedAt(OffsetDateTime.now());
        mockAuthenticatedUser(usuario);

        userService.getUserInfo();
    }

    private static void mockAuthenticatedUser(Usuario usuario) {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(usuario);

        SecurityContextHolder.setContext(securityContext);
    }
}
