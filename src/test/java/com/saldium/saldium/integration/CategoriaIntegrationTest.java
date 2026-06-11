package com.saldium.saldium.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saldium.saldium.dto.categoria.CategoriaRequestDTO;
import com.saldium.saldium.entidades.Categoria;
import com.saldium.saldium.entidades.TipoTransacao;
import com.saldium.saldium.entidades.Transacao;
import com.saldium.saldium.repository.CategoriaRepository;
import com.saldium.saldium.repository.TransacaoRepository;
import com.saldium.saldium.security.user.Role;
import com.saldium.saldium.security.user.UserRepository;
import com.saldium.saldium.security.user.Usuario;
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static com.saldium.saldium.util.categorias.CategoriasCreator.*;
import static com.saldium.saldium.util.usuario.UsuarioCreator.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CategoriaIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private TransacaoRepository transacaoRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanDatabase() {
        transacaoRepository.deleteAll();
        categoriaRepository.deleteAll();
        userRepository.deleteAll();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void save_ShouldSaveCategoria_WhenSuccessfully() throws Exception {
        Usuario usuario = criarUsuarioParaTesteDeIntegracao();
        userRepository.save(usuario);
        autenticarUsuario(usuario);

        CategoriaRequestDTO request = new CategoriaRequestDTO("parcela do carro", TipoTransacao.DESPESA);

        mockMvc.perform(post("/categorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        List<Categoria> categorias = categoriaRepository.findAllAvailableForUser(usuario);

        assertEquals(1, categorias.size());
        assertEquals(TipoTransacao.DESPESA, categorias.get(0).getTipo());
        assertEquals("PARCELA DO CARRO", categorias.get(0).getNome());
    }

    @Test
    void save_ShouldThrowException_WhenCategoriaAlreadyExist() throws Exception {
        Usuario usuario = criarUsuarioParaTesteDeIntegracao();
        userRepository.save(usuario);
        autenticarUsuario(usuario);

        Categoria categoria = new Categoria();
        categoria.setNome("JOGOS");
        categoria.setTipo(TipoTransacao.DESPESA);
        categoria.setUsuario(usuario);
        categoria.setCategoriaDoSistema(false);

        categoriaRepository.save(categoria);

        CategoriaRequestDTO request = new CategoriaRequestDTO("jogos", TipoTransacao.DESPESA);

        mockMvc.perform(post("/categorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Categoria Já Existe"));
    }

    @Test
    void findAll_ShouldReturnListOfAvailableCategoriesForUser_WhenSuccessfullyAndHasRoleUser() throws Exception {
        Usuario usuario = criarUsuarioParaTesteDeIntegracao();
        userRepository.save(usuario);
        autenticarUsuario(usuario);

        Usuario usuario2 = new Usuario();
        usuario2.setNome("user2");
        usuario2.setEmail("user2@email.com");
        usuario2.setSenha("password");
        usuario2.setRole(Role.ROLE_USER);
        usuario2.setCreatedAt(OffsetDateTime.now());
        userRepository.save(usuario2);

        Usuario admin = criarAdminParaTesteDeIntegracao();
        userRepository.save(admin);

        Categoria categoriaUsuario = criarCategoriaDeUsuarioParaTesteDeIntegracao();
        categoriaUsuario.setUsuario(usuario);

        Categoria categoriaUsuario2 = criarCategoriaDeUsuarioParaTesteDeIntegracao();
        categoriaUsuario2.setUsuario(usuario2);

        Categoria categoriaSistema = criarCategoriaDoSistemaParaTesteDeIntegracao();
        categoriaSistema.setUsuario(admin);

        categoriaRepository.save(categoriaUsuario);
        categoriaRepository.save(categoriaUsuario2);
        categoriaRepository.save(categoriaSistema);

        mockMvc.perform(get("/categorias"))
                .andExpect(status().isOk());

        List<Categoria> allAvailableForUser = categoriaRepository.findAllAvailableForUser(usuario);

        assertEquals(2, allAvailableForUser.size());
    }

    @Test
    void findAll_ShouldReturnAllCategories_WhenSuccessfullyAndHasRoleAdmin() throws Exception {
        Usuario admin = criarAdminParaTesteDeIntegracao();
        userRepository.save(admin);
        autenticarUsuario(admin);

        Usuario usuario = criarUsuarioParaTesteDeIntegracao();
        userRepository.save(usuario);

        Usuario usuario2 = new Usuario();
        usuario2.setNome("user2");
        usuario2.setEmail("user2@email.com");
        usuario2.setSenha("password");
        usuario2.setRole(Role.ROLE_USER);
        usuario2.setCreatedAt(OffsetDateTime.now());
        userRepository.save(usuario2);

        Categoria categoriaUsuario = criarCategoriaDeUsuarioParaTesteDeIntegracao();
        categoriaUsuario.setUsuario(usuario);

        Categoria categoriaUsuario2 = criarCategoriaDeUsuarioParaTesteDeIntegracao();
        categoriaUsuario2.setUsuario(usuario2);

        Categoria categoriaSistema = criarCategoriaDoSistemaParaTesteDeIntegracao();
        categoriaSistema.setUsuario(admin);

        categoriaRepository.save(categoriaUsuario);
        categoriaRepository.save(categoriaUsuario2);
        categoriaRepository.save(categoriaSistema);

        mockMvc.perform(get("/categorias"))
                .andExpect(status().isOk());

        List<Categoria> allAvailableForUser = categoriaRepository.findAll();

        assertEquals(3, allAvailableForUser.size());
    }

    @Test
    void findById_ShouldReturnCategory_WhenSuccessfullyAndHasRoleUser() throws Exception {
        Usuario admin = criarAdminParaTesteDeIntegracao();
        userRepository.save(admin);
        autenticarUsuario(admin);

        Usuario usuario = criarUsuarioParaTesteDeIntegracao();
        userRepository.save(usuario);

        Categoria categoriaUsuario = criarCategoriaDeUsuarioParaTesteDeIntegracao();
        categoriaUsuario.setUsuario(usuario);
        categoriaRepository.save(categoriaUsuario);

        mockMvc.perform(get("/categorias/{id}", categoriaUsuario.getId()))
                .andExpect(status().isOk());

        Optional<Categoria> categoria = categoriaRepository.findById(categoriaUsuario.getId());

        assertNotNull(categoria);
        assertEquals(usuario.getId(), categoria.get().getUsuario().getId());
    }

    @Test
    void findById_ShouldReturnCategory_WhenSuccessfullyAndHasRoleAdmin() throws Exception {
        Usuario usuario = criarUsuarioParaTesteDeIntegracao();
        userRepository.save(usuario);
        autenticarUsuario(usuario);

        Categoria categoriaUsuario = criarCategoriaDeUsuarioParaTesteDeIntegracao();
        categoriaUsuario.setUsuario(usuario);
        categoriaRepository.save(categoriaUsuario);

        mockMvc.perform(get("/categorias/{id}", categoriaUsuario.getId()))
                .andExpect(status().isOk());

        Optional<Categoria> categoria = categoriaRepository.findAccessibleById(categoriaUsuario.getId(), usuario);

        assertNotNull(categoria);
        assertEquals(usuario.getId(), categoria.get().getUsuario().getId());
    }

    @Test
    void findById_ShouldReturnNotFound_WhenUserTryFoundAnotherUserCategoria() throws Exception {
        Usuario usuario = criarUsuarioParaTesteDeIntegracao();
        userRepository.save(usuario);
        autenticarUsuario(usuario);

        Usuario usuario2 = new Usuario();
        usuario2.setNome("user2");
        usuario2.setEmail("user2@email.com");
        usuario2.setSenha("password");
        usuario2.setRole(Role.ROLE_USER);
        usuario2.setCreatedAt(OffsetDateTime.now());

        userRepository.save(usuario2);

        Categoria categoriaUsuario = criarCategoriaDeUsuarioParaTesteDeIntegracao();
        categoriaUsuario.setUsuario(usuario);

        Categoria categoriaUsuario2 = criarCategoriaDeUsuarioParaTesteDeIntegracao();
        categoriaUsuario2.setUsuario(usuario2);

        categoriaRepository.save(categoriaUsuario);
        categoriaRepository.save(categoriaUsuario2);

        mockMvc.perform(get("/categorias/{id}", categoriaUsuario2.getId()))
                .andExpect(status().isNotFound());

        categoriaRepository.findAccessibleById(categoriaUsuario.getId(), usuario);
    }

    @Test
    void findById_ShouldReturnNotFound_WhenCategoriaNotFound() throws Exception {
        Usuario usuario = criarUsuarioParaTesteDeIntegracao();
        userRepository.save(usuario);
        autenticarUsuario(usuario);

        mockMvc.perform(get("/categorias/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_ShouldUpdateCategoria_WhenSuccessfully() throws Exception {
        CategoriaRequestDTO request = new CategoriaRequestDTO("parcela do carro", TipoTransacao.DESPESA);

        Usuario usuario = criarUsuarioParaTesteDeIntegracao();
        userRepository.save(usuario);
        autenticarUsuario(usuario);

        Categoria categoria = criarCategoriaDeUsuarioParaTesteDeIntegracao();
        categoria.setUsuario(usuario);
        categoriaRepository.save(categoria);

        Optional<Categoria> categoriaParaAtualizar = categoriaRepository.findByIdAndUsuario(categoria.getId(), usuario);

        categoriaParaAtualizar.ifPresent(c -> c.setNome(request.nome()));
        categoriaParaAtualizar.ifPresent(c -> c.setTipo(request.tipo()));

        Categoria categoriaAtualizada = categoriaRepository.save(categoriaParaAtualizar.get());

        mockMvc.perform(put("/categorias/{id}", categoria.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        assertEquals(request.nome(), categoriaAtualizada.getNome());
        assertEquals(categoria.getId(), categoriaAtualizada.getId());
    }

    @Test
    void update_ShouldThrowException_WhenCategoriaIsInUse() throws Exception {
        CategoriaRequestDTO request = new CategoriaRequestDTO("parcela do carro", TipoTransacao.DESPESA);

        Usuario usuario = criarUsuarioParaTesteDeIntegracao();
        userRepository.save(usuario);
        autenticarUsuario(usuario);

        Categoria categoria = criarCategoriaDeUsuarioParaTesteDeIntegracao();
        categoria.setUsuario(usuario);
        categoriaRepository.save(categoria);

        Transacao transacao = new Transacao();
        transacao.setDescricao("Skin do CS");
        transacao.setValor(new BigDecimal("200"));
        transacao.setTipoTransacao(TipoTransacao.DESPESA);
        transacao.setUsuario(usuario);
        transacao.setCategoria(categoria);
        transacao.setDataCriacao(OffsetDateTime.now());
        transacaoRepository.save(transacao);

        categoriaRepository.findByIdAndUsuario(categoria.getId(), usuario);

        mockMvc.perform(put("/categorias/{id}", categoria.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Categoria Em Uso"));
    }

    @Test
    void delete_ShouldDeleteCategoria_WhenSuccessfully() throws Exception {
        Usuario usuario = criarUsuarioParaTesteDeIntegracao();
        userRepository.save(usuario);
        autenticarUsuario(usuario);

        Categoria categoria = criarCategoriaDeUsuarioParaTesteDeIntegracao();
        categoria.setUsuario(usuario);
        categoriaRepository.save(categoria);

        mockMvc.perform(delete("/categorias/{id}", categoria.getId()))
                .andExpect(status().isNoContent());

        assertTrue(categoriaRepository.findAll().isEmpty());
    }

    @Test
    void delete_ShouldThrowException_WhenCategoriaIsInUse() throws Exception {
        Usuario usuario = criarUsuarioParaTesteDeIntegracao();
        userRepository.save(usuario);
        autenticarUsuario(usuario);

        Categoria categoria = criarCategoriaDeUsuarioParaTesteDeIntegracao();
        categoria.setUsuario(usuario);
        categoriaRepository.save(categoria);

        Transacao transacao = new Transacao();
        transacao.setDescricao("Skin do CS");
        transacao.setValor(new BigDecimal("200"));
        transacao.setTipoTransacao(TipoTransacao.DESPESA);
        transacao.setUsuario(usuario);
        transacao.setCategoria(categoria);
        transacao.setDataCriacao(OffsetDateTime.now());
        transacaoRepository.save(transacao);

        categoriaRepository.findByIdAndUsuario(categoria.getId(), usuario);

        mockMvc.perform(delete("/categorias/{id}", categoria.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Categoria Em Uso"));

        assertEquals(1, categoriaRepository.count());
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
