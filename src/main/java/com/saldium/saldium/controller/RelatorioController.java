package com.saldium.saldium.controller;

import com.saldium.saldium.dto.relatorio.RelatorioCategoriaDTO;
import com.saldium.saldium.dto.relatorio.RelatorioResposeDTO;
import com.saldium.saldium.service.RelatorioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "relatorios")
@RestController
@RequestMapping("/relatorios")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class RelatorioController {
    private final RelatorioService relatorioService;

    @GetMapping("/mes")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Retorna o total das despesas e dos gastos e o saldo do mês")
    public RelatorioResposeDTO relatorioMensal(@RequestParam Integer ano, @RequestParam Integer mes) {
        return relatorioService.relatorioMensal(ano, mes);
    }

    @GetMapping("/ano")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Retorna o total das despesas dos gastos e o saldo do ano")
    public RelatorioResposeDTO relatorioAnual(@RequestParam Integer ano) {
        return relatorioService.relatorioAnual(ano);
    }
    
    @GetMapping("/categoria")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Retorna o total das despesas/renda por categoria em um mês")
    public List<RelatorioCategoriaDTO> relatorioCategoria(@RequestParam Integer ano, @RequestParam Integer mes) {
        return relatorioService.relatorioCategoria(ano, mes);
    }
}
