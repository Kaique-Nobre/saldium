package com.saldium.saldium.service;

import com.saldium.saldium.dto.relatorio.ResumoMesDTO;
import com.saldium.saldium.dto.relatorio.RelatorioAnualResponseDTO;
import com.saldium.saldium.dto.relatorio.RelatorioCategoriaDTO;
import com.saldium.saldium.dto.relatorio.RelatorioResposeDTO;
import com.saldium.saldium.exceptions.BadRequestException;
import com.saldium.saldium.repository.TransacaoRepository;
import com.saldium.saldium.security.user.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RelatorioService {
    private final TransacaoRepository transacaoRepository;

    public RelatorioResposeDTO relatorioMensal(Integer ano, Integer mes) {
        Usuario usuario = getUsuarioAutenticado();

        OffsetDateTime inicio =
                YearMonth.of(ano, mes)
                        .atDay(1)
                        .atStartOfDay()
                        .atOffset(ZoneOffset.UTC);

        OffsetDateTime fim = inicio.plusMonths(1);

        BigDecimal renda = transacaoRepository.totalRenda(usuario.getId(), inicio, fim);
        BigDecimal despesas = transacaoRepository.totalDespesas(usuario.getId(), inicio, fim);
        BigDecimal saldo = renda.subtract(despesas);

        return new RelatorioResposeDTO(renda, despesas, saldo);
    }

    public RelatorioResposeDTO relatorioAnual(Integer ano) {
        Usuario usuario = getUsuarioAutenticado();

        OffsetDateTime inicio =
                Year.of(ano)
                        .atMonth(1)
                        .atDay(1)
                        .atStartOfDay()
                        .atOffset(ZoneOffset.UTC);

        OffsetDateTime fim = inicio.plusYears(1);

        BigDecimal renda = transacaoRepository.totalRenda(usuario.getId(), inicio, fim);
        BigDecimal despesas = transacaoRepository.totalDespesas(usuario.getId(), inicio, fim);
        BigDecimal saldo = renda.subtract(despesas);

        return new RelatorioResposeDTO(renda, despesas, saldo);
    }

    public List<RelatorioAnualResponseDTO> relatorioAnualDetalhado(Integer ano) {
        Usuario usuario = getUsuarioAutenticado();

        List<ResumoMesDTO> relatorio = transacaoRepository.buscarResumoAnual(usuario.getId(), ano);

        return relatorio.stream()
                .map(this::converterParaRelatorio)
                .toList();
    }

    public List<RelatorioCategoriaDTO> relatorioCategoria(Integer ano, Integer mes) {
        Usuario usuario = getUsuarioAutenticado();

        OffsetDateTime inicio =
                YearMonth.of(ano, mes)
                        .atDay(1)
                        .atStartOfDay()
                        .atOffset(ZoneOffset.UTC);

        OffsetDateTime fim = inicio.plusMonths(1);

        return transacaoRepository.totalPorCategoria(usuario.getId(), inicio, fim);
    }

    private static Usuario getUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (Usuario) authentication.getPrincipal();
    }

    private RelatorioAnualResponseDTO converterParaRelatorio(ResumoMesDTO resumo) {
        BigDecimal saldo = resumo.totalRenda().subtract(resumo.totalDespesas());

        return new RelatorioAnualResponseDTO(
                resumo.mes(),
                resumo.totalRenda(),
                resumo.totalDespesas(),
                saldo
        );
    }
}
