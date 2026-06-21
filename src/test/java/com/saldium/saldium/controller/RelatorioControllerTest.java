package com.saldium.saldium.controller;

import com.saldium.saldium.dto.relatorio.RelatorioAnualResponseDTO;
import com.saldium.saldium.dto.relatorio.RelatorioCategoriaDTO;
import com.saldium.saldium.dto.relatorio.RelatorioResposeDTO;
import com.saldium.saldium.entidades.TipoTransacao;
import com.saldium.saldium.exceptions.BadRequestException;
import com.saldium.saldium.security.jwt.JwtAuthenticationFilter;
import com.saldium.saldium.service.RelatorioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RelatorioController.class)
@AutoConfigureMockMvc(addFilters = false)
public class RelatorioControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RelatorioService relatorioService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void relatorioMensal_ShouldReturnRelatorio_WhenSuccessfully() throws Exception {
        RelatorioResposeDTO response = new RelatorioResposeDTO(new BigDecimal("1000"), new BigDecimal("400"), new BigDecimal("400"));

        when(relatorioService.relatorioMensal(anyInt(), anyInt())).thenReturn(response);

        mockMvc.perform(get("/relatorios/mes?ano=2026&mes=6"))
                .andExpect(status().isOk());
    }

    @Test
    void relatorioAnual_ShouldReturnRelatorio_WhenSuccessfully() throws Exception {
        RelatorioResposeDTO response = new RelatorioResposeDTO(new BigDecimal("1000"), new BigDecimal("400"), new BigDecimal("400"));

        when(relatorioService.relatorioAnual(anyInt())).thenReturn(response);

        mockMvc.perform(get("/relatorios/ano?ano=2026"))
                .andExpect(status().isOk());
    }

    @Test
    void relatorioAnualDetalhado_ShouldReturnRelatorio_WhenSuccessfully() throws Exception {
        RelatorioAnualResponseDTO response =
                new RelatorioAnualResponseDTO(1, new BigDecimal("5000"), new BigDecimal("2000"), new BigDecimal("3000"));

        List<RelatorioAnualResponseDTO> list = List.of(response);

        when(relatorioService.relatorioAnualDetalhado(2026)).thenReturn(list);

        mockMvc.perform(get("/relatorios/ano/meses?ano=2026"))
                .andExpect(status().isOk());
    }

    @Test
    void relatorioCategoria_ShouldReturnRelatorio_WhenSuccessfully() throws Exception {
        RelatorioCategoriaDTO categoria1 = new RelatorioCategoriaDTO(false,1L, "ALIMENTAÇÃO", TipoTransacao.DESPESA, new BigDecimal("400"));
        RelatorioCategoriaDTO categoria2 = new RelatorioCategoriaDTO(false,2L, "LAZER", TipoTransacao.DESPESA, new BigDecimal("210"));
        RelatorioCategoriaDTO categoria3 = new RelatorioCategoriaDTO(false,3L, "NETFLIX", TipoTransacao.DESPESA, new BigDecimal("70"));

        List<RelatorioCategoriaDTO> categorias = List.of(categoria1, categoria2, categoria3);

        when(relatorioService.relatorioCategoria(anyInt(), anyInt())).thenReturn(categorias);

        mockMvc.perform(get("/relatorios/categoria?ano=2026&mes=6"))
                .andExpect(status().isOk());
    }
}
