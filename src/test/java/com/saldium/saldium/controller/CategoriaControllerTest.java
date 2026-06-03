package com.saldium.saldium.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saldium.saldium.dto.CategoriaRequestDTO;
import com.saldium.saldium.dto.CategoriaResponseDTO;
import com.saldium.saldium.entidades.TipoCategoria;
import com.saldium.saldium.exceptions.categoria.CategoriaJaExisteException;
import com.saldium.saldium.exceptions.categoria.CategoriaNaoEncontradaException;
import com.saldium.saldium.service.CategoriaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.saldium.saldium.util.categorias.CategoriasCreator.criarCategoriaRequest;
import static com.saldium.saldium.util.categorias.CategoriasCreator.criarCategoriaResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoriaController.class)
public class CategoriaControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoriaService categoriaService;

    @Test
    void save_ShouldReturn201_WhenSuccessfully() throws Exception {
        CategoriaRequestDTO request = criarCategoriaRequest();
        CategoriaResponseDTO response = criarCategoriaResponse();

        when(categoriaService.save(any(CategoriaRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/categorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nome").value("SALÁRIO"))
                .andExpect(jsonPath("$.tipo").value("RENDA"));
    }

    @Test
    void save_ShouldReturnConflict_WhenCategoriaAlreadyExists() throws Exception {
        CategoriaRequestDTO request = criarCategoriaRequest();

        when(categoriaService.save(any(CategoriaRequestDTO.class))).thenThrow(new CategoriaJaExisteException("Categoria Já Existe"));

        mockMvc.perform(post("/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Categoria Já Existe"));
    }

    @Test
    void save_ShouldReturnBadRequest_WhenCategoriaIsInvalid() throws Exception {
        CategoriaRequestDTO request = new CategoriaRequestDTO(null, null);

        mockMvc.perform(post("/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Error"));
        verify(categoriaService, never()).save(any(CategoriaRequestDTO.class));
    }

    @Test
    void findAll_ShouldReturnListOfCategorias() throws Exception {
        CategoriaResponseDTO response = criarCategoriaResponse();

        List<CategoriaResponseDTO> responseList = List.of(response);

        when(categoriaService.findAll()).thenReturn(responseList);

        mockMvc.perform(get("/categorias"))
                .andExpect(status().isOk());
    }

    @Test
    void findById_ShouldReturnCategoria_WhenSuccessfully() throws Exception {
        CategoriaResponseDTO response = criarCategoriaResponse();

        when(categoriaService.findById(anyLong())).thenReturn(response);

        mockMvc.perform(get("/categorias/1"))
                .andExpect(status().isOk());
    }

    @Test
    void findById_ShouldReturnNotFound_WhenCategoriaNotFound() throws Exception {
        when(categoriaService.findById(anyLong()))
                .thenThrow(new CategoriaNaoEncontradaException("Categoria Não Encontrada"));

        mockMvc.perform(get("/categorias/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Categoria Não Encontrada"));;
    }

    @Test
    void update_ShouldReturnOk_WhenSuccessfully() throws Exception {
        CategoriaRequestDTO request = criarCategoriaRequest();
        CategoriaResponseDTO response = criarCategoriaResponse();

        when(categoriaService.update(eq(1L), any(CategoriaRequestDTO.class))).thenReturn(response);

        mockMvc.perform(put("/categorias/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nome").value("SALÁRIO"))
                .andExpect(jsonPath("$.tipo").value("RENDA"));
    }

    @Test
    void update_ShouldReturnNotFound_WhenCategoriaNotFound() throws Exception {
        CategoriaRequestDTO request = criarCategoriaRequest();
        when(categoriaService
                .update(eq(1L), any(CategoriaRequestDTO.class)))
                .thenThrow(new CategoriaNaoEncontradaException("Categoria Não Encontrada"));

        mockMvc.perform(put("/categorias/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Categoria Não Encontrada"));;
    }

    @Test
    void update_ShouldReturnBadRequest_WhenCategoriaIsInvalid() throws Exception {
        CategoriaRequestDTO request = new CategoriaRequestDTO(null, null);

        mockMvc.perform(put("/categorias/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Error"));
        verify(categoriaService, never()).save(any(CategoriaRequestDTO.class));
    }

    @Test
    void delete_ShouldReturnNoContent_WhenSuccessfully() throws Exception {
        mockMvc.perform(delete("/categorias/1"))
                .andExpect(status().isNoContent());
    }
}
