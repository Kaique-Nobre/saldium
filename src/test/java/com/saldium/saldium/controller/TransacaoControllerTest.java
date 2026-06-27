package com.saldium.saldium.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saldium.saldium.dto.transacao.TransacaoFiltroDTO;
import com.saldium.saldium.dto.transacao.TransacaoRequestDTO;
import com.saldium.saldium.dto.transacao.TransacaoResponseDTO;
import com.saldium.saldium.entidades.Categoria;
import com.saldium.saldium.entidades.TipoTransacao;
import com.saldium.saldium.exceptions.categoria.CategoriaIncompativelException;
import com.saldium.saldium.exceptions.categoria.CategoriaNaoEncontradaException;
import com.saldium.saldium.exceptions.transacao.TransacaoNaoEncontradaException;
import com.saldium.saldium.security.jwt.JwtAuthenticationFilter;
import com.saldium.saldium.service.TransacaoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.saldium.saldium.util.transacao.TransacaoCreator.criarTransacaoRequestDTO;
import static com.saldium.saldium.util.transacao.TransacaoCreator.criarTransacaoResponseDTO;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransacaoController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TransacaoControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TransacaoService transacaoService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void save_ShouldReturnCreated_WhenSuccessfully() throws Exception {
        TransacaoRequestDTO request = criarTransacaoRequestDTO();
        TransacaoResponseDTO response = criarTransacaoResponseDTO();

        when(transacaoService.save(any(TransacaoRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/transacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void save_ShouldReturnNotFound_WhenCategoriaNotFound() throws Exception {
        TransacaoRequestDTO request = criarTransacaoRequestDTO();

        when(transacaoService.save(any(TransacaoRequestDTO.class)))
                .thenThrow(new CategoriaNaoEncontradaException("Categoria não encontrada"));

        mockMvc.perform(post("/transacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Categoria não encontrada"));
    }

    @Test
    void save_ShouldReturnBadRequest_WhenCategoriaIsInvalid() throws Exception {
        Categoria categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNome("MERCADO");
        categoria.setTipo(TipoTransacao.DESPESA);

        TransacaoRequestDTO request = criarTransacaoRequestDTO();

        when(transacaoService.save(any(TransacaoRequestDTO.class)))
                .thenThrow(new CategoriaIncompativelException("Categoria incompativel com o tipo da transação"));

        mockMvc.perform(post("/transacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Categoria incompativel com o tipo da transação"));
    }

    @Test
    void findAll_ShouldReturnAllTransacoes_WhenSuccessfully() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        TransacaoFiltroDTO filtroDTO = new TransacaoFiltroDTO(null, null, null, null);
        TransacaoResponseDTO response = criarTransacaoResponseDTO();

        Page<TransacaoResponseDTO> transacoes = new PageImpl<>(List.of(response), pageable, 1);

        when(transacaoService.findAll(filtroDTO, pageable)).thenReturn(transacoes);

        mockMvc.perform(get("/transacoes"))
                .andExpect(status().isOk());
    }

    @Test
    void findById_ShouldReturnTransacao_WhenSuccessfully() throws Exception {
        TransacaoResponseDTO response = criarTransacaoResponseDTO();

        when(transacaoService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/transacoes/1"))
                .andExpect(status().isOk());
    }

    @Test
    void findById_ShouldReturnNotFound_WhenTransacaoNotFound() throws Exception {
        when(transacaoService.findById(1L)).thenThrow(new TransacaoNaoEncontradaException("Transação não encontrada"));

        mockMvc.perform(get("/transacoes/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Transação não encontrada"));
    }

    @Test
    void update_ShouldReturnOk_WhenSuccessfully() throws Exception {
        TransacaoRequestDTO request = criarTransacaoRequestDTO();
        TransacaoResponseDTO response = criarTransacaoResponseDTO();

        when(transacaoService.update(eq(1L), any(TransacaoRequestDTO.class))).thenReturn(response);

        mockMvc.perform(put("/transacoes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void update_ShouldReturnNotFound_WhenTransacaoNotFound() throws Exception {
        TransacaoRequestDTO request = criarTransacaoRequestDTO();

        when(transacaoService.update(eq(1L), any(TransacaoRequestDTO.class)))
                .thenThrow(new TransacaoNaoEncontradaException("Transação não encontrada"));

        mockMvc.perform(put("/transacoes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Transação não encontrada"));;
    }

    @Test
    void update_ShouldReturnNotFound_WhenCategoriaNotFound() throws Exception {
        TransacaoRequestDTO request = criarTransacaoRequestDTO();

        when(transacaoService.update(eq(1L), any(TransacaoRequestDTO.class)))
                .thenThrow(new CategoriaNaoEncontradaException("Categoria não encontrada"));

        mockMvc.perform(put("/transacoes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Categoria não encontrada"));;
    }

    @Test
    void update_ShouldReturnBadRequest_WhenCategoriaIsIncompatible() throws Exception {
        TransacaoRequestDTO request = criarTransacaoRequestDTO();

        when(transacaoService.update(eq(1L), any(TransacaoRequestDTO.class)))
                .thenThrow(new CategoriaIncompativelException("Categoria incompatível com tipo da transação"));

        mockMvc.perform(put("/transacoes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Categoria incompatível com tipo da transação"));;
    }

    @Test
    void delete_ShouldReturnNoContent_WhenSuccessfully() throws Exception {
        doNothing().when(transacaoService).delete(anyLong());

        mockMvc.perform(delete("/transacoes/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_ShouldReturnNotFound_WhenTransacaoNotFound() throws Exception {
        doThrow(new TransacaoNaoEncontradaException("Transacao não encontrada")).when(transacaoService).delete(anyLong());

        mockMvc.perform(delete("/transacoes/1"))
                .andExpect(status().isNotFound());
    }
}
