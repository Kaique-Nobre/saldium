package com.saldium.saldium.controller;

import com.saldium.saldium.dto.relatorio.RelatorioCategoriaDTO;
import com.saldium.saldium.dto.relatorio.RelatorioResposeDTO;
import com.saldium.saldium.service.RelatorioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/relatorios")
@RequiredArgsConstructor
public class RelatorioController {
    private final RelatorioService relatorioService;

    @GetMapping("/mes")
    @PreAuthorize("isAuthenticated()")
    public RelatorioResposeDTO relatorioMensal(@RequestParam Integer ano, @RequestParam Integer mes) {
        return relatorioService.relatorioMensal(ano, mes);
    }

    @GetMapping("/ano")
    @PreAuthorize("isAuthenticated()")
    public RelatorioResposeDTO relatorioAnual(@RequestParam Integer ano) {
        return relatorioService.relatorioAnual(ano);
    }
    
    @GetMapping("/categoria")
    @PreAuthorize("isAuthenticated()") 
    public List<RelatorioCategoriaDTO> relatorioCategoria(@RequestParam Integer ano, @RequestParam Integer mes) {
        return relatorioService.relatorioCategoria(ano, mes);
    }
}
