package com.saldium.saldium.service;

import com.saldium.saldium.dto.transacao.TransacaoRequestDTO;
import com.saldium.saldium.dto.transacao.TransacaoResponseDTO;
import com.saldium.saldium.entidades.Categoria;
import com.saldium.saldium.entidades.TipoTransacao;
import com.saldium.saldium.entidades.Transacao;
import com.saldium.saldium.exceptions.categoria.CategoriaIncompativelException;
import com.saldium.saldium.exceptions.categoria.CategoriaNaoEncontradaException;
import com.saldium.saldium.exceptions.transacao.TransacaoNaoEncontradaException;
import com.saldium.saldium.mapper.TransacaoMapper;
import com.saldium.saldium.repository.CategoriaRepository;
import com.saldium.saldium.repository.TransacaoRepository;
import com.saldium.saldium.security.user.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static com.saldium.saldium.util.categorias.CategoriasCreator.criarCategoria;
import static com.saldium.saldium.util.transacao.TransacaoCreator.*;
import static com.saldium.saldium.util.usuario.UsuarioCreator.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class TransacaoServiceTest {
    @Mock
    private TransacaoRepository transacaoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private TransacaoMapper transacaoMapper;

    @Mock
    private SecurityContextHolder securityContextHolder;

    @InjectMocks
    private TransacaoService transacaoService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void save_ShouldSaveTransacao_WhenSuccessfully() throws Exception {
        TransacaoRequestDTO request = criarTransacaoRequestDTO();

        Categoria categoria = criarCategoria();

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("user@email.com");

        mockAuthenticatedUser(usuario);

        when(categoriaRepository.findById(request.categoria_id())).thenReturn(Optional.of(categoria));

        Transacao transacao = new Transacao();
        transacao.setDescricao(request.descricao());
        transacao.setValor(request.valor());
        transacao.setTipoTransacao(request.tipoTransacao());
        transacao.setCategoria(categoria);
        transacao.setUsuario(usuario);
        transacao.setDataCriacao(OffsetDateTime.now());

        when(transacaoRepository.save(any(Transacao.class))).thenReturn(transacao);

        TransacaoResponseDTO response = new TransacaoResponseDTO(
                1L,
                request.descricao(),
                request.valor(),
                request.tipoTransacao(),
                usuario.getEmail(),
                categoria.getNome(),
                OffsetDateTime.now()
        );

        when(transacaoMapper.toResponseDTO(any(Transacao.class))).thenReturn(response);

        TransacaoResponseDTO transacaoSalva = transacaoService.save(request);

        assertNotNull(transacaoSalva);
        assertEquals(request.descricao(), transacaoSalva.descricao());
        assertEquals("user@email.com", transacaoSalva.usuario());

        ArgumentCaptor<Transacao> captor = ArgumentCaptor.forClass(Transacao.class);

        verify(transacaoRepository).save(captor.capture());

        Transacao transacaoCapturada = captor.getValue();

        assertEquals(categoria, transacaoCapturada.getCategoria());
        assertEquals(usuario.getEmail(), transacaoCapturada.getUsuario().getEmail());

        verify(transacaoMapper).toResponseDTO(any(Transacao.class));
    }

    @Test
    void save_ShouldThrowException_WhenCategoryNotFound() throws Exception {
        TransacaoRequestDTO request = criarTransacaoRequestDTO();

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("user@email.com");

        mockAuthenticatedUser(usuario);

        when(categoriaRepository.findById(request.categoria_id())).thenReturn(Optional.empty());

        assertThrows(CategoriaNaoEncontradaException.class, () -> transacaoService.save(request));

        verify(transacaoRepository, never()).save(any(Transacao.class));
    }

    @Test
    void save_ShouldThrowException_WhenCategoriaTypeIsIncompatible() throws Exception {
        TransacaoRequestDTO request = criarTransacaoRequestDTO();

        Categoria categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNome("MERCADO");
        categoria.setTipo(TipoTransacao.DESPESA);

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("user@email.com");

        mockAuthenticatedUser(usuario);

        when(categoriaRepository.findById(request.categoria_id())).thenReturn(Optional.of(categoria));
        assertThrows(CategoriaIncompativelException.class, () -> transacaoService.save(request));

        verify(transacaoRepository, never()).save(any(Transacao.class));
    }

    @Test
    void findAll_ShouldReturnAllTransacoes_WhenHasRoleAdmin() throws Exception {
        Usuario admin = criarAdmin();
        Usuario usuario = criarUsuario();

        Categoria categoria = criarCategoria();

        Transacao transacao = criarTransacao();
        transacao.setCategoria(categoria);
        transacao.setUsuario(usuario);

        TransacaoResponseDTO response = criarTransacaoResponseDTO();

        List<Transacao> transacoes = List.of(transacao);
        List<TransacaoResponseDTO> responseDTOs = List.of(response);

        mockAuthenticatedUser(admin);

        when(transacaoRepository.findAll()).thenReturn(transacoes);
        when(transacaoMapper.toResponseList(transacoes)).thenReturn(responseDTOs);

        List<TransacaoResponseDTO> responseList = transacaoService.findAll();

        assertTrue(responseList.containsAll(responseDTOs));
        assertEquals(1L, responseList.get(0).id());

        verify(transacaoRepository).findAll();
        verify(transacaoMapper).toResponseList(transacoes);
    }

    @Test
    void findAll_ShouldReturnAllUserTransacoes_WhenHasRoleUser() throws Exception {
        Usuario usuario1 = criarUsuario();

        Usuario usuario2 = criarUsuario();
        usuario2.setId(2L);
        usuario2.setEmail("user2@email.com");

        Categoria categoria = criarCategoria();

        Transacao transacaoUsuario1 = criarTransacao();
        transacaoUsuario1.setCategoria(categoria);
        transacaoUsuario1.setUsuario(usuario1);

        Transacao transacaoUsuario2 = criarTransacao();
        transacaoUsuario2.setCategoria(categoria);
        transacaoUsuario2.setUsuario(usuario2);

        TransacaoResponseDTO response1 = criarTransacaoResponseDTO();

        List<Transacao> transacoes = List.of(transacaoUsuario1);
        List<TransacaoResponseDTO> responseDTOs = List.of(response1);

        mockAuthenticatedUser(usuario1);

        when(transacaoRepository.findAllByUsuario(usuario1)).thenReturn(transacoes);
        when(transacaoMapper.toResponseList(transacoes)).thenReturn(responseDTOs);

        List<TransacaoResponseDTO> responseList = transacaoService.findAll();

        assertTrue(responseList.containsAll(responseDTOs));
        assertEquals(usuario1.getEmail(), responseList.get(0).usuario());

        verify(transacaoRepository).findAllByUsuario(usuario1);
        verify(transacaoRepository, never()).findAllByUsuario(usuario2);
        verify(transacaoRepository, never()).findAll();
        verify(transacaoMapper).toResponseList(transacoes);
    }

    @Test
    void findById_ReturnTransacao_WhenHasRoleUser() throws Exception {
        Usuario usuario = criarUsuario();

        mockAuthenticatedUser(usuario);

        Categoria categoria = criarCategoria();

        Transacao transacao = criarTransacao();
        transacao.setCategoria(categoria);
        transacao.setUsuario(usuario);

        TransacaoResponseDTO responseDTO = criarTransacaoResponseDTO();

        when(transacaoRepository.findByIdAndUsuario(1L, usuario)).thenReturn(Optional.of(transacao));
        when(transacaoMapper.toResponseDTO(transacao)).thenReturn(responseDTO);

        TransacaoResponseDTO response = transacaoService.findById(1L);

        assertNotNull(response);
        assertEquals(usuario.getEmail(), response.usuario());

        verify(transacaoRepository).findByIdAndUsuario(1L, usuario);
        verify(transacaoRepository, never()).findById(anyLong());
        verify(transacaoMapper).toResponseDTO(transacao);
    }

    @Test
    void findById_ReturnTransacao_WhenHasRoleAdmin() throws Exception {
        Usuario admin = criarAdmin();
        Usuario usuario = criarUsuario();

        mockAuthenticatedUser(admin);

        Categoria categoria = criarCategoria();

        Transacao transacao = criarTransacao();
        transacao.setCategoria(categoria);
        transacao.setUsuario(usuario);

        TransacaoResponseDTO responseDTO = criarTransacaoResponseDTO();

        when(transacaoRepository.findById(1L)).thenReturn(Optional.of(transacao));
        when(transacaoMapper.toResponseDTO(transacao)).thenReturn(responseDTO);

        TransacaoResponseDTO response = transacaoService.findById(1L);

        assertNotNull(response);
        assertEquals(usuario.getEmail(), response.usuario());

        verify(transacaoRepository).findById(1L);
        verify(transacaoRepository, never()).findByIdAndUsuario(anyLong(), any(Usuario.class));
        verify(transacaoMapper).toResponseDTO(transacao);
    }

    @Test
    void update_ShouldUpdateTransacao_WhenHasRoleUser() throws Exception {
        TransacaoRequestDTO request = criarTransacaoRequestDTO();

        Usuario usuario = criarUsuario();

        mockAuthenticatedUser(usuario);

        Categoria categoria = criarCategoria();

        Transacao transacao = criarTransacao();
        transacao.setCategoria(categoria);
        transacao.setUsuario(usuario);

        TransacaoResponseDTO responseDTO = criarTransacaoResponseDTO();

        when(transacaoRepository.findByIdAndUsuario(1L, usuario)).thenReturn(Optional.of(transacao));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(transacaoRepository.save(any(Transacao.class))).thenReturn(transacao);
        when(transacaoMapper.toResponseDTO(transacao)).thenReturn(responseDTO);

        TransacaoResponseDTO transacaoAtualizada = transacaoService.update(1L, request);

        assertNotNull(transacaoAtualizada);
        assertEquals(usuario.getEmail(), responseDTO.usuario());
        assertEquals(categoria.getNome(), responseDTO.categoria());

        verify(transacaoRepository).findByIdAndUsuario(1L, usuario);
        verify(transacaoRepository, never()).findById(anyLong());
        verify(transacaoRepository).save(any(Transacao.class));
        verify(categoriaRepository).findById(1L);
        verify(transacaoMapper).toResponseDTO(transacao);
    }

    @Test
    void update_ShouldUpdateTransacao_WhenHasRoleAdmin() throws Exception {
        TransacaoRequestDTO request = criarTransacaoRequestDTO();

        Usuario admin = criarAdmin();

        Usuario usuario = criarUsuario();

        mockAuthenticatedUser(admin);

        Categoria categoria = criarCategoria();

        Transacao transacao = criarTransacao();
        transacao.setCategoria(categoria);
        transacao.setUsuario(usuario);

        TransacaoResponseDTO responseDTO = criarTransacaoResponseDTO();

        when(transacaoRepository.findById(1L)).thenReturn(Optional.of(transacao));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(transacaoRepository.save(any(Transacao.class))).thenReturn(transacao);
        when(transacaoMapper.toResponseDTO(transacao)).thenReturn(responseDTO);

        TransacaoResponseDTO transacaoAtualizada = transacaoService.update(1L, request);

        assertNotNull(transacaoAtualizada);
        assertEquals(usuario.getEmail(), responseDTO.usuario());
        assertEquals(categoria.getNome(), responseDTO.categoria());

        verify(transacaoRepository, never()).findByIdAndUsuario(1L, usuario);
        verify(transacaoRepository).findById(1L);
        verify(transacaoRepository).save(any(Transacao.class));
        verify(categoriaRepository).findById(1L);
        verify(transacaoMapper).toResponseDTO(transacao);
    }

    @Test
    void update_ShouldThrowException_WhenTransacaoNotFound() throws Exception {
        TransacaoRequestDTO request = criarTransacaoRequestDTO();

        Usuario usuario = criarUsuario();

        mockAuthenticatedUser(usuario);

        when(transacaoRepository.findByIdAndUsuario(1L, usuario)).thenReturn(Optional.empty());

        assertThrows(TransacaoNaoEncontradaException.class, () -> transacaoService.update(1L, request));

        verify(transacaoRepository).findByIdAndUsuario(1L, usuario);
        verify(transacaoRepository, never()).save(any(Transacao.class));
        verify(transacaoMapper, never()).toResponseDTO(any(Transacao.class));
    }

    @Test
    void delete_ShouldDeleteTransacao_WhenHasRoleUser() throws Exception {
        Usuario usuario = criarUsuario();

        mockAuthenticatedUser(usuario);

        Categoria categoria = criarCategoria();

        Transacao transacao = criarTransacao();
        transacao.setCategoria(categoria);
        transacao.setUsuario(usuario);

        when(transacaoRepository.findByIdAndUsuario(1L, usuario)).thenReturn(Optional.of(transacao));

        transacaoService.delete(1L);

        verify(transacaoRepository).findByIdAndUsuario(1L, usuario);
        verify(transacaoRepository, never()).findById(1L);
        verify(transacaoRepository).delete(transacao);
    }

    @Test
    void delete_ShouldDeleteTransacao_WhenHasRoleAdmin() throws Exception {
        Usuario admin = criarAdmin();

        Usuario usuario = criarUsuario();

        mockAuthenticatedUser(admin);

        Categoria categoria = criarCategoria();

        Transacao transacao = criarTransacao();
        transacao.setCategoria(categoria);
        transacao.setUsuario(usuario);

        when(transacaoRepository.findById(1L)).thenReturn(Optional.of(transacao));

        transacaoService.delete(1L);

        verify(transacaoRepository, never()).findByIdAndUsuario(1L, usuario);
        verify(transacaoRepository).findById(1L);
        verify(transacaoRepository).delete(transacao);
    }

    @Test
    void delete_ShouldThrowException_WhenTransacaoNotFound() throws Exception {
        Usuario usuario = criarUsuario();

        mockAuthenticatedUser(usuario);

        when(transacaoRepository.findByIdAndUsuario(1L, usuario)).thenReturn(Optional.empty());

        assertThrows(TransacaoNaoEncontradaException.class, () -> transacaoService.delete(1L));
    }

    private static void mockAuthenticatedUser(Usuario usuario) {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(usuario);

        SecurityContextHolder.setContext(securityContext);
    }
}
