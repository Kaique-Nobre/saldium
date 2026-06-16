package com.saldium.saldium.service;

import com.saldium.saldium.dto.categoria.CategoriaRequestDTO;
import com.saldium.saldium.dto.categoria.CategoriaResponseDTO;
import com.saldium.saldium.entidades.Categoria;
import com.saldium.saldium.entidades.TipoTransacao;
import com.saldium.saldium.exceptions.categoria.CategoriaEmUsoException;
import com.saldium.saldium.exceptions.categoria.CategoriaJaExisteException;
import com.saldium.saldium.exceptions.categoria.CategoriaNaoEncontradaException;
import com.saldium.saldium.mapper.CategoriaMapper;
import com.saldium.saldium.repository.CategoriaRepository;
import com.saldium.saldium.repository.TransacaoRepository;
import com.saldium.saldium.security.user.Role;
import com.saldium.saldium.security.user.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static com.saldium.saldium.util.categorias.CategoriasCreator.*;
import static com.saldium.saldium.util.usuario.UsuarioCreator.criarAdmin;
import static com.saldium.saldium.util.usuario.UsuarioCreator.criarUsuario;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoriaServiceTest {
    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private TransacaoRepository transacaoRepository;

    @Mock
    private CategoriaMapper categoriaMapper;

    @Mock
    private SecurityContextHolder securityContextHolder;

    @InjectMocks
    private CategoriaService categoriaService;

    @Test
    public void save_ShouldSaveCategoria_WhenHasRoleAdmin() throws Exception {
        CategoriaRequestDTO request = criarCategoriaRequest();

        mockAuthenticatedUser(criarAdmin());

        Categoria categoria = criarCategoriaSistema();

        CategoriaResponseDTO response = criarCategoriaResponse();

        when(categoriaRepository.existsByNome(request.nome().toUpperCase())).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoria);
        when(categoriaMapper.toDTO(any(Categoria.class))).thenReturn(response);

        CategoriaResponseDTO savedCategoria = categoriaService.save(request);

        assertNotNull(savedCategoria);
        assertEquals("SALÁRIO", savedCategoria.nome());
        assertEquals("RENDA", savedCategoria.tipo());
        assertEquals(Role.ROLE_ADMIN, categoria.getUsuario().getRole());
        assertTrue(categoria.isCategoriaDoSistema());
    }

    @Test
    public void save_ShouldSaveCategoria_WhenHasRoleUser() throws Exception {
        CategoriaRequestDTO request = criarCategoriaRequest();

        mockAuthenticatedUser(criarUsuario());

        Categoria categoria = criarCategoriaDeUsuario();

        CategoriaResponseDTO response = criarCategoriaResponse();

        when(categoriaRepository.existsByNome(request.nome().toUpperCase())).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoria);
        when(categoriaMapper.toDTO(any(Categoria.class))).thenReturn(response);

        CategoriaResponseDTO savedCategoria = categoriaService.save(request);

        assertNotNull(savedCategoria);
        assertEquals("SALÁRIO", savedCategoria.nome());
        assertEquals("RENDA", savedCategoria.tipo());
        assertEquals(Role.ROLE_USER, categoria.getUsuario().getRole());
        assertFalse(categoria.isCategoriaDoSistema());
    }

    @Test
    public void save_ShouldReturnConflict_WhenCategoriaAlreadyExists() throws Exception {
        CategoriaRequestDTO request = criarCategoriaRequest();

        mockAuthenticatedUser(criarAdmin());

        Categoria categoria = criarCategoriaSistema();

        when(categoriaRepository.existsByNome(request.nome().toUpperCase())).thenReturn(true);
        when(categoriaRepository.findByNome(request.nome().toUpperCase())).thenReturn(Optional.of(categoria));
        assertThrows(CategoriaJaExisteException.class, () -> categoriaService.save(request));

        verify(categoriaRepository, never()).save(any(Categoria.class));
    }

    @Test
    public void findAll_ShouldReturnAllCategorias_WhenHasRoleAdmin() throws Exception {
        mockAuthenticatedUser(criarAdmin());

        Categoria  categoria = criarCategoriaSistema();
        List<Categoria> categorias = List.of(categoria);

        CategoriaResponseDTO response = criarCategoriaResponse();

        when(categoriaMapper.toDTO(any(Categoria.class))).thenReturn(response);
        when(categoriaRepository.findAll()).thenReturn(categorias);

        List<CategoriaResponseDTO> all = categoriaService.findAll();

        assertNotNull(all);
        assertEquals(1, all.size());
        assertEquals(1L, all.get(0).id());
        assertEquals("SALÁRIO", all.get(0).nome());

        verify(categoriaRepository).findAll();
        verify(categoriaRepository, never()).findAllAvailableForUser(any(Usuario.class));
    }

    @Test
    public void findAll_ShouldReturnListOfCategoriesThemselves_WhenHasRoleUser() throws Exception {
        mockAuthenticatedUser(criarUsuario());

        Categoria  categoriaSistema = criarCategoriaSistema();
        categoriaSistema.setId(1L);

        Categoria  categoriaUsuario = criarCategoriaDeUsuario();
        categoriaUsuario.setId(2L);

        List<Categoria> categorias = List.of(categoriaSistema, categoriaUsuario);

        CategoriaResponseDTO responseCategoriaSistema = new CategoriaResponseDTO(1L, "SALÁRIO", "RENDA", true);
        CategoriaResponseDTO responseCategoriaUsuario = new CategoriaResponseDTO(2L, "ACADEMIA", "DESPESA", false);

        when(categoriaMapper.toDTO(categoriaSistema)).thenReturn(responseCategoriaSistema);
        when(categoriaMapper.toDTO(categoriaUsuario)).thenReturn(responseCategoriaUsuario);
        when(categoriaRepository.findAllAvailableForUser(any(Usuario.class))).thenReturn(categorias);

        List<CategoriaResponseDTO> all = categoriaService.findAll();
        System.out.println(all);

        assertNotNull(all);
        assertEquals(2, all.size());
        assertEquals(1L, all.get(0).id());
        assertEquals(2L, all.get(1).id());
        assertEquals("SALÁRIO", all.get(0).nome());
        assertEquals("ACADEMIA", all.get(1).nome());

        verify(categoriaRepository, never()).findAll();
        verify(categoriaRepository).findAllAvailableForUser(any(Usuario.class));
    }

    @Test
    public void findById_ShouldReturnCategoria_WhenHasRoleAdmin() throws Exception {
        mockAuthenticatedUser(criarAdmin());

        Categoria categoria = criarCategoriaSistema();
        CategoriaResponseDTO response = criarCategoriaResponse();

        when(categoriaRepository.findById(anyLong())).thenReturn(Optional.of(categoria));
        when(categoriaMapper.toDTO(any(Categoria.class))).thenReturn(response);

        CategoriaResponseDTO categoriaResponse = categoriaService.findById(categoria.getId());

        assertNotNull(categoriaResponse);
        assertEquals(1L, categoriaResponse.id());
        assertEquals("SALÁRIO", categoriaResponse.nome());
        assertEquals("RENDA", categoriaResponse.tipo());

        verify(categoriaRepository).findById(anyLong());
        verify(categoriaRepository, never()).findAccessibleById(anyLong(), any(Usuario.class));
    }

    @Test
    public void findById_ShouldReturnCategoria_WhenHasRoleUser() throws Exception {
        Usuario usuario = criarUsuario();
        mockAuthenticatedUser(usuario);

        Categoria categoria = criarCategoriaDeUsuario();
        CategoriaResponseDTO response = criarCategoriaResponse();

        when(categoriaRepository.findAccessibleById(1L, usuario)).thenReturn(Optional.of(categoria));
        when(categoriaMapper.toDTO(any(Categoria.class))).thenReturn(response);

        CategoriaResponseDTO categoriaResponse = categoriaService.findById(categoria.getId());

        assertNotNull(categoriaResponse);
        assertEquals(1L, categoriaResponse.id());
        assertEquals("SALÁRIO", categoriaResponse.nome());
        assertEquals("RENDA", categoriaResponse.tipo());

        verify(categoriaRepository, never()).findById(anyLong());
        verify(categoriaRepository).findAccessibleById(anyLong(), any(Usuario.class));
    }

    @Test
    public void findById_ShouldThrowsException_WhenCategoriaNotFoundAndHasRoleAdmin() throws Exception {
        mockAuthenticatedUser(criarAdmin());

        when(categoriaRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(CategoriaNaoEncontradaException.class, () -> categoriaService.findById(1L));

        verify(categoriaRepository, never()).findAccessibleById(anyLong(), any(Usuario.class));
    }

    @Test
    public void findById_ShouldThrowsException_WhenCategoriaNotFoundAndHasRoleUser() throws Exception {
        Usuario usuario = criarUsuario();
        mockAuthenticatedUser(usuario);

        when(categoriaRepository.findAccessibleById(1L, usuario)).thenReturn(Optional.empty());

        assertThrows(CategoriaNaoEncontradaException.class, () -> categoriaService.findById(1L));

        verify(categoriaRepository).findAccessibleById(1L, usuario);
        verify(categoriaRepository, never()).findById(anyLong());
    }


    @Test
    public void update_ShouldUpdateCategoria_WhenHasRoleAdmin() throws Exception {
        CategoriaRequestDTO request = new CategoriaRequestDTO("freelance", TipoTransacao.RENDA);
        CategoriaResponseDTO response = new CategoriaResponseDTO(1L, "FREELANCE", "RENDA", true);

        mockAuthenticatedUser(criarAdmin());

        Categoria categoriaParaAtualizar = criarCategoriaSistema();

        Categoria categoriaAtualizada = new Categoria();
        categoriaAtualizada.setId(1L);
        categoriaAtualizada.setNome(request.nome().toUpperCase());
        categoriaAtualizada.setTipo(request.tipo());

        when(categoriaRepository.findById(anyLong())).thenReturn(Optional.of(categoriaParaAtualizar));
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoriaAtualizada);
        when(categoriaMapper.toDTO(any(Categoria.class))).thenReturn(response);

        CategoriaResponseDTO update = categoriaService.update(1L, request);

        assertNotNull(update);
        assertEquals(1L, update.id());
        assertEquals("FREELANCE", update.nome());
        assertEquals("RENDA", update.tipo());

        verify(categoriaRepository).findById(anyLong());
        verify(categoriaRepository, never()).findByIdAndUsuario(anyLong(), any(Usuario.class));
        verify(categoriaRepository).save(any(Categoria.class));
    }

    @Test
    public void update_ShouldUpdateCategoria_WhenHasRoleUser() throws Exception {
        CategoriaRequestDTO request = new CategoriaRequestDTO("freelance", TipoTransacao.RENDA);
        CategoriaResponseDTO response = new CategoriaResponseDTO(1L, "FREELANCE", "RENDA", false);

        Usuario usuario = criarUsuario();

        mockAuthenticatedUser(usuario);

        Categoria categoriaParaAtualizar = criarCategoriaDeUsuario();
        categoriaParaAtualizar.setUsuario(usuario);

        Categoria categoriaAtualizada = new Categoria();
        categoriaAtualizada.setId(1L);
        categoriaAtualizada.setNome(request.nome().toUpperCase());
        categoriaAtualizada.setTipo(request.tipo());

        when(categoriaRepository.findByIdAndUsuario(1L, usuario)).thenReturn(Optional.of(categoriaParaAtualizar));
        when(transacaoRepository.existsByCategoriaId(anyLong())).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoriaAtualizada);
        when(categoriaMapper.toDTO(any(Categoria.class))).thenReturn(response);

        CategoriaResponseDTO update = categoriaService.update(1L, request);

        assertNotNull(update);
        assertEquals(1L, update.id());
        assertEquals("FREELANCE", update.nome());
        assertEquals("RENDA", update.tipo());

        verify(categoriaRepository, never()).findById(anyLong());
        verify(categoriaRepository).findByIdAndUsuario(anyLong(), any(Usuario.class));
        verify(transacaoRepository).existsByCategoriaId(anyLong());
        verify(categoriaRepository).save(any(Categoria.class));
    }

    @Test
    public void update_ShouldThrowExceptiom_WhenCategoriaIsInUse() throws Exception {
        CategoriaRequestDTO request = new CategoriaRequestDTO("freelance", TipoTransacao.RENDA);

        Usuario usuario = criarUsuario();

        mockAuthenticatedUser(usuario);

        Categoria categoriaParaAtualizar = criarCategoriaDeUsuario();
        categoriaParaAtualizar.setUsuario(usuario);

        Categoria categoriaAtualizada = new Categoria();
        categoriaAtualizada.setId(1L);
        categoriaAtualizada.setNome(request.nome().toUpperCase());
        categoriaAtualizada.setTipo(request.tipo());

        when(categoriaRepository.findByIdAndUsuario(1L, usuario)).thenReturn(Optional.of(categoriaParaAtualizar));
        when(transacaoRepository.existsByCategoriaId(anyLong())).thenReturn(true);

        assertThrows(CategoriaEmUsoException.class, () -> categoriaService.update(1L, request));


        verify(categoriaRepository, never()).findById(anyLong());
        verify(categoriaRepository).findByIdAndUsuario(anyLong(), any(Usuario.class));
        verify(transacaoRepository).existsByCategoriaId(anyLong());
        verify(categoriaRepository, never()).save(any(Categoria.class));
    }

    @Test
    public void update_ShouldThrowsException_WhenCategoriaNotFound() throws Exception {
        mockAuthenticatedUser(criarAdmin());

        CategoriaRequestDTO request = new CategoriaRequestDTO("freelance", TipoTransacao.RENDA);

        when(categoriaRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(CategoriaNaoEncontradaException.class, () -> categoriaService.update(1L, request));

        verify(categoriaRepository).findById(anyLong());
        verify(categoriaRepository, never()).findAccessibleById(anyLong(), any(Usuario.class));
    }

    @Test
    public void delete_ShouldDeleteCategoria_WhenHasRoleAdmin() throws Exception {
        mockAuthenticatedUser(criarAdmin());

        Categoria categoria = criarCategoriaSistema();

        when(categoriaRepository.findById(anyLong())).thenReturn(Optional.of(categoria));

        categoriaService.delete(1L);

        assertEquals(0, categoriaRepository.count());

        verify(categoriaRepository).delete(categoria);
        verify(categoriaRepository).findById(anyLong());
        verify(categoriaRepository, never()).findByIdAndUsuario(anyLong(), any(Usuario.class));
    }

    @Test
    public void delete_ShouldDeleteCategoria_WhenHasRoleUser() throws Exception {
        Usuario usuario = criarUsuario();
        mockAuthenticatedUser(usuario);

        Categoria categoria = criarCategoriaDeUsuario();

        when(categoriaRepository.findByIdAndUsuario(1L, usuario)).thenReturn(Optional.of(categoria));
        when(transacaoRepository.existsByCategoriaId(anyLong())).thenReturn(false);

        categoriaService.delete(1L);

        assertEquals(0, categoriaRepository.count());

        verify(categoriaRepository).delete(categoria);
        verify(categoriaRepository, never()).findById(anyLong());
        verify(transacaoRepository).existsByCategoriaId(anyLong());
        verify(categoriaRepository).findByIdAndUsuario(1L, usuario);
    }

    @Test
    public void delete_ShouldThrowException_WhenCategoriaIsInUse() throws Exception {
        Usuario usuario = criarUsuario();
        mockAuthenticatedUser(usuario);

        Categoria categoria = criarCategoriaDeUsuario();

        when(categoriaRepository.findByIdAndUsuario(1L, usuario)).thenReturn(Optional.of(categoria));
        when(transacaoRepository.existsByCategoriaId(anyLong())).thenReturn(true);

        assertThrows(CategoriaEmUsoException.class, () -> categoriaService.delete(1L));

        verify(categoriaRepository, never()).delete(categoria);
        verify(categoriaRepository, never()).findById(anyLong());
        verify(transacaoRepository).existsByCategoriaId(anyLong());
        verify(categoriaRepository).findByIdAndUsuario(1L, usuario);
    }

    @Test
    public void delete_ShouldThrowsException_WhenCategoriaNotFoundAndHasRoleAdmin() throws Exception {
        mockAuthenticatedUser(criarAdmin());

        when(categoriaRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(CategoriaNaoEncontradaException.class, () -> categoriaService.delete(1L));

        verify(categoriaRepository).findById(anyLong());
        verify(categoriaRepository, never()).findByIdAndUsuario(anyLong(), any(Usuario.class));
    }

    @Test
    public void delete_ShouldThrowsException_WhenCategoriaNotFoundAndHasRoleUser() throws Exception {
        Usuario usuario = criarUsuario();

        mockAuthenticatedUser(usuario);

        when(categoriaRepository.findByIdAndUsuario(1L, usuario)).thenReturn(Optional.empty());

        assertThrows(CategoriaNaoEncontradaException.class, () -> categoriaService.delete(1L));

        verify(categoriaRepository, never()).findById(anyLong());
        verify(categoriaRepository).findByIdAndUsuario(1L, usuario);
    }

    private static void mockAuthenticatedUser(Usuario usuario) {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(usuario);

        SecurityContextHolder.setContext(securityContext);
    }
}
