package com.saldium.saldium.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saldium.saldium.dto.transacao.TransacaoFiltroDTO;
import com.saldium.saldium.dto.transacao.TransacaoRequestDTO;
import com.saldium.saldium.dto.transacao.TransacaoSpecification;
import com.saldium.saldium.entidades.Categoria;
import com.saldium.saldium.entidades.TipoTransacao;
import com.saldium.saldium.entidades.Transacao;
import com.saldium.saldium.repository.CategoriaRepository;
import com.saldium.saldium.repository.TransacaoRepository;
import com.saldium.saldium.security.refreshToken.RefreshTokenRepository;
import com.saldium.saldium.security.user.Role;
import com.saldium.saldium.security.user.UserRepository;
import com.saldium.saldium.security.user.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static com.saldium.saldium.util.categorias.CategoriasCreator.criarCategoriaDeUsuarioParaTesteDeIntegracao;
import static com.saldium.saldium.util.categorias.CategoriasCreator.criarCategoriaDoSistemaParaTesteDeIntegracao;
import static com.saldium.saldium.util.transacao.TransacaoCreator.criarTransacaoParaTesteDeIntegracao;
import static com.saldium.saldium.util.usuario.UsuarioCreator.criarAdminParaTesteDeIntegracao;
import static com.saldium.saldium.util.usuario.UsuarioCreator.criarUsuarioParaTesteDeIntegracao;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TransacaoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private TransacaoRepository transacaoRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanDatabase() {
        transacaoRepository.deleteAll();
        categoriaRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void save_ShouldSaveTransacao_WhenSuccessfully() throws Exception {
        Usuario usuario = criarUsuarioParaTesteDeIntegracao();
        autenticarUsuario(usuario);
        userRepository.save(usuario);

        Categoria categoria = criarCategoriaDeUsuarioParaTesteDeIntegracao();
        categoria.setUsuario(usuario);
        categoriaRepository.save(categoria);

        TransacaoRequestDTO request =
                new TransacaoRequestDTO(
                        "Skin do CS",
                        new BigDecimal("120"),
                        TipoTransacao.DESPESA,
                        LocalDate.now(),
                        categoria.getId());

        mockMvc.perform(post("/transacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        List<Transacao> transacoes = transacaoRepository.findAll();

        assertEquals(1, transacoes.size());
        assertEquals(TipoTransacao.DESPESA, transacoes.get(0).getTipoTransacao());
        assertEquals(categoria.getId(), transacoes.get(0).getCategoria().getId());
        assertEquals(usuario.getId(), transacoes.get(0).getUsuario().getId());
    }

    @Test
    void save_ShouldReturnNotFund_WhenCategoriaNotFound() throws Exception {
        Usuario usuario = criarUsuarioParaTesteDeIntegracao();
        autenticarUsuario(usuario);
        userRepository.save(usuario);

        Categoria categoria = criarCategoriaDeUsuarioParaTesteDeIntegracao();
        categoria.setUsuario(usuario);
        categoriaRepository.save(categoria);

        TransacaoRequestDTO request =
                new TransacaoRequestDTO(
                        "Venda de site",
                        new BigDecimal("4000"),
                        TipoTransacao.RENDA,
                        LocalDate.now(),
                        categoria.getId());

        mockMvc.perform(post("/transacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Categoria Incompatível"));

        List<Transacao> transacoes = transacaoRepository.findAll();

        assertEquals(0, transacoes.size());
    }

    @Test
    void save_ShouldReturnBadRequest_WhenCategoriaTypeIsIncompatible() throws Exception {
        Usuario usuario = criarUsuarioParaTesteDeIntegracao();
        autenticarUsuario(usuario);
        userRepository.save(usuario);

        TransacaoRequestDTO request =
                new TransacaoRequestDTO(
                        "Skin do CS",
                        new BigDecimal("120"),
                        TipoTransacao.DESPESA,
                        LocalDate.now(),
                        99L);

        mockMvc.perform(post("/transacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Categoria Não Encontrada"));

        List<Transacao> transacoes = transacaoRepository.findAll();

        assertEquals(0, transacoes.size());
    }

    @Test
    void findAll_ShouldReturnListOfAvailableTransacoesForUser_WhenSuccessfullyAndHasRoleUser() throws Exception {
        TransacaoFiltroDTO filtro = new TransacaoFiltroDTO(null, null, null, null);
        Pageable pageable = PageRequest.of(0, 10);

        Usuario usuario = criarUsuarioParaTesteDeIntegracao();
        autenticarUsuario(usuario);
        userRepository.save(usuario);

        Specification<Transacao> spec =
                TransacaoSpecification.comFiltros(filtro, usuario);

        Usuario usuario2 = new Usuario();
        usuario2.setNome("user2");
        usuario2.setEmail("user2@email.com");
        usuario2.setSenha("password");
        usuario2.setRole(Role.ROLE_USER);
        usuario2.setCreatedAt(OffsetDateTime.now());
        userRepository.save(usuario2);

        Usuario admin = criarAdminParaTesteDeIntegracao();
        userRepository.save(admin);

        Categoria categoria = criarCategoriaDoSistemaParaTesteDeIntegracao();
        categoria.setUsuario(admin);
        categoriaRepository.save(categoria);

        Transacao transacaoUsuario1 = criarTransacaoParaTesteDeIntegracao();
        transacaoUsuario1.setUsuario(usuario);
        transacaoUsuario1.setCategoria(categoria);
        transacaoRepository.save(transacaoUsuario1);

        Transacao transacaoUsuario2 = criarTransacaoParaTesteDeIntegracao();
        transacaoUsuario2.setUsuario(usuario2);
        transacaoUsuario2.setCategoria(categoria);
        transacaoRepository.save(transacaoUsuario2);

        mockMvc.perform(get("/transacoes"))
                .andExpect(status().isOk());

        Page<Transacao> transacoes = transacaoRepository.findAll(spec, pageable);

        assertEquals(1, transacoes.toList().size());
        assertEquals(usuario.getId(), transacoes.toList().get(0).getUsuario().getId());
    }

    @Test
    void findAll_ShouldReturnAllTransacoes_WhenSuccessfullyAndHasRoleAdmin() throws Exception {
        TransacaoFiltroDTO filtro = new TransacaoFiltroDTO(null, null, null, null);
        Pageable pageable = PageRequest.of(0, 10);

        Usuario usuario = criarUsuarioParaTesteDeIntegracao();
        autenticarUsuario(usuario);
        userRepository.save(usuario);

        Usuario usuario2 = new Usuario();
        usuario2.setNome("user2");
        usuario2.setEmail("user2@email.com");
        usuario2.setSenha("password");
        usuario2.setRole(Role.ROLE_USER);
        usuario2.setCreatedAt(OffsetDateTime.now());
        userRepository.save(usuario2);

        Usuario admin = criarAdminParaTesteDeIntegracao();
        userRepository.save(admin);

        Specification<Transacao> spec =
                TransacaoSpecification.comFiltros(filtro, admin);

        Categoria categoria = criarCategoriaDoSistemaParaTesteDeIntegracao();
        categoria.setUsuario(admin);
        categoriaRepository.save(categoria);

        Transacao transacaoUsuario1 = criarTransacaoParaTesteDeIntegracao();
        transacaoUsuario1.setUsuario(usuario);
        transacaoUsuario1.setCategoria(categoria);
        transacaoRepository.save(transacaoUsuario1);

        Transacao transacaoUsuario2 = criarTransacaoParaTesteDeIntegracao();
        transacaoUsuario2.setUsuario(usuario2);
        transacaoUsuario2.setCategoria(categoria);
        transacaoRepository.save(transacaoUsuario2);

        mockMvc.perform(get("/transacoes"))
                .andExpect(status().isOk());

        Page<Transacao> transacoes = transacaoRepository.findAll(spec, pageable);

        assertEquals(2, transacoes.toList().size());
        assertEquals(usuario.getId(), transacoes.toList().get(0).getUsuario().getId());
        assertEquals(usuario2.getId(), transacoes.toList().get(1).getUsuario().getId());
    }

    @Test
    void findById_ShouldReturnTransacao_WhenSuccessfully() throws Exception {
        Usuario usuario = criarUsuarioParaTesteDeIntegracao();
        autenticarUsuario(usuario);
        userRepository.save(usuario);

        Usuario admin = criarAdminParaTesteDeIntegracao();
        userRepository.save(admin);

        Categoria categoria = criarCategoriaDoSistemaParaTesteDeIntegracao();
        categoria.setUsuario(admin);
        categoriaRepository.save(categoria);

        Transacao transacaoUsuario = criarTransacaoParaTesteDeIntegracao();
        transacaoUsuario.setUsuario(usuario);
        transacaoUsuario.setCategoria(categoria);
        transacaoRepository.save(transacaoUsuario);

        mockMvc.perform(get("/transacoes/{id}", transacaoUsuario.getId()))
                .andExpect(status().isOk());

        Optional<Transacao> transacao = transacaoRepository.findByIdAndUsuario(transacaoUsuario.getId(), usuario);

        assertNotNull(transacao);
        assertEquals(usuario.getId(), transacao.get().getUsuario().getId());
    }

    @Test
    void findById_ShouldReturnNotFound_WhenUserTryFoundAnotherUserTransacao() throws Exception {
        Usuario usuario = criarUsuarioParaTesteDeIntegracao();
        autenticarUsuario(usuario);
        userRepository.save(usuario);

        Usuario usuario2 = new Usuario();
        usuario2.setNome("user2");
        usuario2.setEmail("user2@email.com");
        usuario2.setSenha("password");
        usuario2.setRole(Role.ROLE_USER);
        usuario2.setCreatedAt(OffsetDateTime.now());
        userRepository.save(usuario2);

        Usuario admin = criarAdminParaTesteDeIntegracao();
        userRepository.save(admin);

        Categoria categoria = criarCategoriaDoSistemaParaTesteDeIntegracao();
        categoria.setUsuario(admin);
        categoriaRepository.save(categoria);

        Transacao transacaoUsuario2 = criarTransacaoParaTesteDeIntegracao();
        transacaoUsuario2.setUsuario(usuario2);
        transacaoUsuario2.setCategoria(categoria);
        transacaoRepository.save(transacaoUsuario2);

        mockMvc.perform(get("/transacoes/{id}", transacaoUsuario2.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Transação Não Encontrada"));
    }

    @Test
    void update_ShouldUpdateTransacao_WhenSuccessfully() throws Exception {
        Usuario usuario = criarUsuarioParaTesteDeIntegracao();
        autenticarUsuario(usuario);
        userRepository.save(usuario);

        Categoria categoria = criarCategoriaDeUsuarioParaTesteDeIntegracao();
        categoria.setUsuario(usuario);
        categoriaRepository.save(categoria);

        Transacao transacaoUsuario = criarTransacaoParaTesteDeIntegracao();
        transacaoUsuario.setUsuario(usuario);
        transacaoUsuario.setCategoria(categoria);
        transacaoRepository.save(transacaoUsuario);

        TransacaoRequestDTO request =
                new TransacaoRequestDTO(
                        "Skin do CS",
                        new BigDecimal("120"),
                        TipoTransacao.DESPESA,
                        LocalDate.now(),
                        categoria.getId());

        Optional<Transacao> transacao = transacaoRepository.findByIdAndUsuario(transacaoUsuario.getId(), usuario);

        transacao.ifPresent(t -> t.setDescricao(request.descricao()));
        transacao.ifPresent(t -> t.setValor(request.valor()));

        Transacao transacaoAtualizada = transacaoRepository.save(transacao.get());

        mockMvc.perform(put("/transacoes/{id}", transacaoUsuario.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        assertEquals(request.descricao(), transacaoAtualizada.getDescricao());
        assertEquals(request.valor(), transacaoAtualizada.getValor());
    }

    @Test
    void update_ShouldReturnNotFound_WhenTransacaoNotFound() throws Exception {
        Usuario usuario = criarUsuarioParaTesteDeIntegracao();
        autenticarUsuario(usuario);
        userRepository.save(usuario);

        Categoria categoria = criarCategoriaDeUsuarioParaTesteDeIntegracao();
        categoria.setUsuario(usuario);
        categoriaRepository.save(categoria);

        TransacaoRequestDTO request =
                new TransacaoRequestDTO(
                        "Skin do CS",
                        new BigDecimal("120"),
                        TipoTransacao.DESPESA,
                        LocalDate.now(),
                        categoria.getId());

        transacaoRepository.findByIdAndUsuario(99L, usuario);

        mockMvc.perform(put("/transacoes/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Transação Não Encontrada"));
    }

    @Test
    void delete_ShouldDeleteTransacao_WhenSuccessfully() throws Exception {
        Usuario usuario = criarUsuarioParaTesteDeIntegracao();
        autenticarUsuario(usuario);
        userRepository.save(usuario);

        Categoria categoria = criarCategoriaDeUsuarioParaTesteDeIntegracao();
        categoria.setUsuario(usuario);
        categoriaRepository.save(categoria);

        Transacao transacaoUsuario = criarTransacaoParaTesteDeIntegracao();
        transacaoUsuario.setUsuario(usuario);
        transacaoUsuario.setCategoria(categoria);
        transacaoRepository.save(transacaoUsuario);

        mockMvc.perform(delete("/transacoes/{id}", transacaoUsuario.getId()))
                .andExpect(status().isNoContent());

        List<Transacao> all = transacaoRepository.findAll();

        assertEquals(0, all.size());
    }

    @Test
    void delete_ShouldReturnNotFound_WhenTransacaoNotFound() throws Exception {
        Usuario usuario = criarUsuarioParaTesteDeIntegracao();
        autenticarUsuario(usuario);
        userRepository.save(usuario);

        mockMvc.perform(delete("/transacoes/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Transação Não Encontrada"));;
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
