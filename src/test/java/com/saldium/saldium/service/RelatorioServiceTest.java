package com.saldium.saldium.service;

import com.saldium.saldium.dto.relatorio.RelatorioAnualResponseDTO;
import com.saldium.saldium.dto.relatorio.RelatorioCategoriaDTO;
import com.saldium.saldium.dto.relatorio.RelatorioResposeDTO;
import com.saldium.saldium.dto.relatorio.ResumoMesDTO;
import com.saldium.saldium.exceptions.BadRequestException;
import com.saldium.saldium.repository.TransacaoRepository;
import com.saldium.saldium.security.user.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RelatorioServiceTest {
    @Mock
    private TransacaoRepository transacaoRepository;

    @Mock
    private SecurityContextHolder securityContextHolder;

    @InjectMocks
    private RelatorioService relatorioService;

    @Test
    public void relatorioMensal_ShouldReturnRelatorioDoMes_WhenSuccessfully() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("usuario@email.com");

        mockAuthenticatedUser(usuario);

        BigDecimal renda = new BigDecimal("1000");
        BigDecimal despesas = new BigDecimal("400");

        OffsetDateTime inicio =
                YearMonth.of(2026, 6)
                        .atDay(1)
                        .atStartOfDay()
                        .atOffset(ZoneOffset.UTC);

        OffsetDateTime fim = inicio.plusMonths(1);

        when(transacaoRepository.totalRenda(usuario.getId(), inicio, fim)).thenReturn(renda);
        when(transacaoRepository.totalDespesas(usuario.getId(), inicio, fim)).thenReturn(despesas);

        RelatorioResposeDTO relatorioRespose = relatorioService.relatorioMensal(2026, 6);

        assertNotNull(relatorioRespose);
        assertEquals(renda, relatorioRespose.totalRenda());

        verify(transacaoRepository).totalRenda(usuario.getId(), inicio, fim);
        verify(transacaoRepository).totalDespesas(usuario.getId(), inicio, fim);
    }

    @Test
    public void relatorioMensal_ShouldThrowBadRequest_WhenDataIsInvalid() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("usuario@email.com");

        mockAuthenticatedUser(usuario);

        assertThrows(BadRequestException.class, () -> relatorioService.relatorioMensal(anoInvalido(), 9));

        verify(transacaoRepository, never()).totalRenda(anyLong(), any(), any());
        verify(transacaoRepository, never()).totalDespesas(anyLong(), any(), any());
    }

    @Test
    public void relatorioAnual_ShouldReturnRelatorioDoAno_WhenSuccessfully() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("usuario@email.com");

        mockAuthenticatedUser(usuario);

        BigDecimal renda = new BigDecimal("1000");
        BigDecimal despesas = new BigDecimal("400");

        OffsetDateTime inicio =
                Year.of(2026)
                        .atMonth(1)
                        .atDay(1)
                        .atStartOfDay()
                        .atOffset(ZoneOffset.UTC);

        OffsetDateTime fim = inicio.plusYears(1);

        when(transacaoRepository.totalRenda(usuario.getId(), inicio, fim)).thenReturn(renda);
        when(transacaoRepository.totalDespesas(usuario.getId(), inicio, fim)).thenReturn(despesas);

        RelatorioResposeDTO relatorioRespose = relatorioService.relatorioAnual(2026);

        assertNotNull(relatorioRespose);
        assertEquals(renda, relatorioRespose.totalRenda());

        verify(transacaoRepository).totalRenda(usuario.getId(), inicio, fim);
        verify(transacaoRepository).totalDespesas(usuario.getId(), inicio, fim);
    }

    @Test
    public void relatorioAnual_ShouldThrowBadRequest_WhenYearIsInvalid() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("usuario@email.com");

        mockAuthenticatedUser(usuario);

        assertThrows(BadRequestException.class, () -> relatorioService.relatorioAnual(anoInvalido()));

        verify(transacaoRepository, never()).totalRenda(anyLong(), any(), any());
        verify(transacaoRepository, never()).totalDespesas(anyLong(), any(), any());
    }

    @Test
    public void relatorioAnualDetalhado_ShouldReturnRelatorioDoAnoDetalhado_WhenSuccessfully() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("usuario@email.com");

        mockAuthenticatedUser(usuario);

        RelatorioAnualResponseDTO relatorio =
                new RelatorioAnualResponseDTO(1, new BigDecimal("5000"), new BigDecimal("2000"), new BigDecimal("3000"));

        ResumoMesDTO resumo = new ResumoMesDTO(1, new BigDecimal("5000"), new BigDecimal("2000"));

        List<RelatorioAnualResponseDTO> relatorios = List.of(relatorio);

        List<ResumoMesDTO> resumos = List.of(resumo);

        when(transacaoRepository.buscarResumoAnual(usuario.getId(), Year.now().getValue())).thenReturn(resumos);

        List<RelatorioAnualResponseDTO> response =
                relatorioService.relatorioAnualDetalhado(Year.now().getValue());

        assertNotNull(response);
        assertEquals(1, response.get(0).mes());

        verify(transacaoRepository).buscarResumoAnual(usuario.getId(), Year.now().getValue());
    }

    @Test
    public void relatorioAnualDetalhado_ShouldThrowBadRequest_WhenYearIsInvalid() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("usuario@email.com");

        mockAuthenticatedUser(usuario);

        assertThrows(BadRequestException.class, () -> relatorioService.relatorioAnualDetalhado(anoInvalido()));

        verify(transacaoRepository, never()).buscarResumoAnual(usuario.getId(), Year.now().getValue());
    }

    @Test
    public void relatorioCategoria_ShouldReturnListOfRelatoriosByCategoria_WhenSuccessfully() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("usuario@email.com");

        mockAuthenticatedUser(usuario);

        RelatorioCategoriaDTO categoria1 = new RelatorioCategoriaDTO("ALIMENTAÇÃO", new BigDecimal("400"));
        RelatorioCategoriaDTO categoria2 = new RelatorioCategoriaDTO("LAZER", new BigDecimal("210"));
        RelatorioCategoriaDTO categoria3 = new RelatorioCategoriaDTO("NETFLIX", new BigDecimal("70"));

        List<RelatorioCategoriaDTO> categorias = List.of(categoria1, categoria2, categoria3);

        OffsetDateTime inicio =
                YearMonth.of(2026, 6)
                        .atDay(1)
                        .atStartOfDay()
                        .atOffset(ZoneOffset.UTC);

        OffsetDateTime fim = inicio.plusMonths(1);

        when(transacaoRepository.totalPorCategoria(usuario.getId(), inicio, fim)).thenReturn(categorias);

        List<RelatorioCategoriaDTO> responseList = relatorioService.relatorioCategoria(2026, 6);

        assertNotNull(responseList);
        assertEquals(categoria1, responseList.get(0));

        verify(transacaoRepository).totalPorCategoria(usuario.getId(), inicio, fim);
    }

    @Test
    public void relatorioCategoria_ShouldThrowBadRequest_WhenDataIsInvalid() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("usuario@email.com");

        mockAuthenticatedUser(usuario);

        assertThrows(BadRequestException.class, () -> relatorioService.relatorioCategoria(anoInvalido(), 7));

        verify(transacaoRepository, never()).totalPorCategoria(any(), any(), any());
    }

    private static void mockAuthenticatedUser(Usuario usuario) {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(usuario);

        SecurityContextHolder.setContext(securityContext);
    }

    private static int anoInvalido() {
        return Year.now().plusYears(5).getValue();
    }
}
