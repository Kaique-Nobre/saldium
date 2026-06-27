package com.saldium.saldium.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saldium.saldium.entidades.Categoria;
import com.saldium.saldium.entidades.Transacao;
import com.saldium.saldium.repository.CategoriaRepository;
import com.saldium.saldium.repository.TransacaoRepository;
import com.saldium.saldium.security.auth.dto.DeletarContaRequestDTO;
import com.saldium.saldium.security.jwt.JwtService;
import com.saldium.saldium.security.passwordResetToken.PasswordResetTokenRepository;
import com.saldium.saldium.security.passwordResetToken.PasswordResetTokenService;
import com.saldium.saldium.security.refreshToken.RefreshToken;
import com.saldium.saldium.security.refreshToken.RefreshTokenRepository;
import com.saldium.saldium.security.user.UserRepository;
import com.saldium.saldium.security.user.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
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

import static com.saldium.saldium.util.categorias.CategoriasCreator.criarCategoriaDeUsuarioParaTesteDeIntegracao;
import static com.saldium.saldium.util.transacao.TransacaoCreator.criarTransacaoParaTesteDeIntegracao;
import static com.saldium.saldium.util.usuario.UsuarioCreator.criarUsuarioParaTesteDeIntegracao;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserIntegrationTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TransacaoRepository transacaoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private PasswordResetTokenService passwordResetTokenService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanDatabase() {
        transacaoRepository.deleteAll();
        categoriaRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        passwordResetTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deletarUsuario_ShouldDeleteUsuario_WhenSuccessfully() throws Exception {
        Usuario usuario = criarUsuarioParaTesteDeIntegracao();
        usuario.setEmailVerificado(true);
        usuario.setSenha(passwordEncoder.encode("password"));
        userRepository.save(usuario);
        autenticarUsuario(usuario);

        Categoria categoria = criarCategoriaDeUsuarioParaTesteDeIntegracao();
        categoria.setUsuario(usuario);
        categoriaRepository.save(categoria);

        Transacao transacao = criarTransacaoParaTesteDeIntegracao();
        transacao.setUsuario(usuario);
        transacao.setCategoria(categoria);
        transacaoRepository.save(transacao);

        passwordResetTokenService.createPasswordResetToken(usuario);

        String refreshToken = jwtService.generateRefreshToken(usuario);

        RefreshToken refreshTokenEntity =
                RefreshToken.builder()
                        .token(refreshToken)
                        .usuario(usuario)
                        .expiraEm(jwtService.getExpirationTime(refreshToken))
                        .revogado(false)
                        .build();
        refreshTokenRepository.save(refreshTokenEntity);

        DeletarContaRequestDTO request = new DeletarContaRequestDTO("password");

        mockMvc.perform(delete("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        assertEquals(0, transacaoRepository.count());
        assertEquals(0, categoriaRepository.count());
        assertEquals(0, refreshTokenRepository.count());
        assertEquals(0, passwordResetTokenRepository.count());
    }

    @Test
    void deletarUsuario_ShouldReturnBadRequest_WhenUserPasswordIsWrong() throws Exception {
        Usuario usuario = criarUsuarioParaTesteDeIntegracao();
        usuario.setEmailVerificado(true);
        usuario.setSenha(passwordEncoder.encode("password"));
        userRepository.save(usuario);
        autenticarUsuario(usuario);

        Categoria categoria = criarCategoriaDeUsuarioParaTesteDeIntegracao();
        categoria.setUsuario(usuario);
        categoriaRepository.save(categoria);

        Transacao transacao = criarTransacaoParaTesteDeIntegracao();
        transacao.setUsuario(usuario);
        transacao.setCategoria(categoria);
        transacaoRepository.save(transacao);

        passwordResetTokenService.createPasswordResetToken(usuario);

        String refreshToken = jwtService.generateRefreshToken(usuario);

        RefreshToken refreshTokenEntity =
                RefreshToken.builder()
                        .token(refreshToken)
                        .usuario(usuario)
                        .expiraEm(jwtService.getExpirationTime(refreshToken))
                        .revogado(false)
                        .build();
        refreshTokenRepository.save(refreshTokenEntity);

        DeletarContaRequestDTO request = new DeletarContaRequestDTO("wrong-password");

        mockMvc.perform(delete("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        assertEquals(1, transacaoRepository.count());
        assertEquals(1, categoriaRepository.count());
        assertEquals(1, refreshTokenRepository.count());
        assertEquals(1, passwordResetTokenRepository.count());
    }

    @Test
    void getUserInfo_ShouldReturnUserInfo_WhenSuccessfully() throws Exception {
        Usuario usuario = criarUsuarioParaTesteDeIntegracao();
        usuario.setEmailVerificado(true);
        usuario.setSenha(passwordEncoder.encode("password"));
        userRepository.save(usuario);
        autenticarUsuario(usuario);

        mockMvc.perform(get("/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value(usuario.getNome()))
                .andExpect(jsonPath("$.email").value(usuario.getEmail()));
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
