package com.saldium.saldium.service;

import com.saldium.saldium.dto.categoria.CategoriaRequestDTO;
import com.saldium.saldium.dto.categoria.CategoriaResponseDTO;
import com.saldium.saldium.entidades.Categoria;
import com.saldium.saldium.entidades.TipoTransacao;
import com.saldium.saldium.exceptions.categoria.CategoriaJaExisteException;
import com.saldium.saldium.exceptions.categoria.CategoriaNaoEncontradaException;
import com.saldium.saldium.mapper.CategoriaMapper;
import com.saldium.saldium.repository.CategoriaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.saldium.saldium.util.categorias.CategoriasCreator.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoriaServiceTest {
    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private CategoriaMapper categoriaMapper;

    @InjectMocks
    private CategoriaService categoriaService;

    @Test
    public void save_ShouldSaveCategoria_WhenSuccessfully() throws Exception {
        CategoriaRequestDTO request = criarCategoriaRequest();
        Categoria categoria = criarCategoria();
        CategoriaResponseDTO response = criarCategoriaResponse();

        when(categoriaRepository.existsByNome(request.nome().toUpperCase())).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoria);
        when(categoriaMapper.toDTO(any(Categoria.class))).thenReturn(response);

        CategoriaResponseDTO savedCategoria = categoriaService.save(request);

        assertNotNull(savedCategoria);
        assertEquals("SALÁRIO", savedCategoria.nome());
        assertEquals("RENDA", savedCategoria.tipo());
    }

    @Test
    public void save_ShouldReturnConflict_WhenCategoriaAlreadyExists() throws Exception {
        CategoriaRequestDTO request = criarCategoriaRequest();

        when(categoriaRepository.existsByNome(request.nome().toUpperCase())).thenReturn(true);
        assertThrows(CategoriaJaExisteException.class, () -> categoriaService.save(request));

        verify(categoriaRepository, never()).save(any(Categoria.class));
    }

    @Test
    public void findAll_ShouldReturnListOfCategorias_WhenSuccessfully() throws Exception {
        Categoria  categoria = criarCategoria();
        List<Categoria> categorias = List.of(categoria);

        CategoriaResponseDTO response = criarCategoriaResponse();

        when(categoriaMapper.toDTO(any(Categoria.class))).thenReturn(response);
        when(categoriaRepository.findAll()).thenReturn(categorias);

        List<CategoriaResponseDTO> all = categoriaService.findAll();

        assertNotNull(all);
        assertEquals(1, all.size());
        assertEquals(1L, all.get(0).id());
        assertEquals("SALÁRIO", all.get(0).nome());
    }

    @Test
    public void findById_ShouldReturnCategoria_WhenSuccessfully() throws Exception {
        Categoria categoria = criarCategoria();
        CategoriaResponseDTO response = criarCategoriaResponse();

        when(categoriaRepository.findById(anyLong())).thenReturn(Optional.of(categoria));
        when(categoriaMapper.toDTO(any(Categoria.class))).thenReturn(response);

        CategoriaResponseDTO categoriaResponse = categoriaService.findById(categoria.getId());

        assertNotNull(categoriaResponse);
        assertEquals(1L, categoriaResponse.id());
        assertEquals("SALÁRIO", categoriaResponse.nome());
        assertEquals("RENDA", categoriaResponse.tipo());
    }

    @Test
    public void findById_ShouldThrowsException_WhenCategoriaNotFound() throws Exception {
        when(categoriaRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(CategoriaNaoEncontradaException.class, () -> categoriaService.findById(1L));
    }

    @Test
    public void update_ShouldUpdateCategoria_WhenSuccessfully() throws Exception {
        CategoriaRequestDTO request = new CategoriaRequestDTO("freelance", TipoTransacao.RENDA);
        CategoriaResponseDTO response = new CategoriaResponseDTO(1L, "FREELANCE", "RENDA");

        Categoria categoriaParaAtualizar = criarCategoria();

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
        verify(categoriaRepository).save(any(Categoria.class));
    }

    @Test
    public void update_ShouldThrowsException_WhenCategoriaNotFound() throws Exception {
        CategoriaRequestDTO request = new CategoriaRequestDTO("freelance", TipoTransacao.RENDA);

        when(categoriaRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(CategoriaNaoEncontradaException.class, () -> categoriaService.update(1L, request));
    }

    @Test
    public void delete_ShouldDeleteCategoria_WhenSuccessfully() throws Exception {
        Categoria categoria = criarCategoria();

        when(categoriaRepository.findById(anyLong())).thenReturn(Optional.of(categoria));

        categoriaService.delete(1L);

        assertEquals(0, categoriaRepository.count());

        verify(categoriaRepository).deleteById(1L);
    }

    @Test
    public void delete_ShouldThrowsException_WhenCategoriaNotFound() throws Exception {
        when(categoriaRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(CategoriaNaoEncontradaException.class, () -> categoriaService.delete(anyLong()));
    }
}
