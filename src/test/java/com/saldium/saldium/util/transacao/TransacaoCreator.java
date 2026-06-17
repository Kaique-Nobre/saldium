package com.saldium.saldium.util.transacao;

import com.saldium.saldium.dto.transacao.TransacaoRequestDTO;
import com.saldium.saldium.dto.transacao.TransacaoResponseDTO;
import com.saldium.saldium.entidades.TipoTransacao;
import com.saldium.saldium.entidades.Transacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public class TransacaoCreator {
    public static TransacaoRequestDTO criarTransacaoRequestDTO() {
        return new TransacaoRequestDTO("Salário dia 5", new BigDecimal("1100"), TipoTransacao.RENDA, LocalDate.now(), 1L);
    }

    public static TransacaoResponseDTO criarTransacaoResponseDTO() {
        return new TransacaoResponseDTO(
                1L,
                "Salário dia 5",
                new BigDecimal("1100"),
                TipoTransacao.RENDA,
                "user@email.com",
                "SALÁRIO",
                1L,
                LocalDate.now(),
                OffsetDateTime.now()
        );
    }

    public static Transacao criarTransacao() {
        Transacao transacao = new Transacao();
        transacao.setDescricao("Salário dia 5");
        transacao.setValor(new BigDecimal("1100"));
        transacao.setTipoTransacao(TipoTransacao.RENDA);
        transacao.setDataTransacao(LocalDate.now());
        transacao.setDataCriacao(OffsetDateTime.now());
        return transacao;
    }

    public static Transacao criarTransacaoParaTesteDeIntegracao() {
        Transacao transacao = new Transacao();
        transacao.setDescricao("TV nova");
        transacao.setValor(new BigDecimal("3000"));
        transacao.setTipoTransacao(TipoTransacao.DESPESA);
        transacao.setDataTransacao(LocalDate.now());
        transacao.setDataCriacao(OffsetDateTime.now());
        return transacao;
    }
}
