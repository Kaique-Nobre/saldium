package com.saldium.saldium.repository;

import com.saldium.saldium.dto.relatorio.ResumoMesDTO;
import com.saldium.saldium.dto.relatorio.RelatorioCategoriaDTO;
import com.saldium.saldium.entidades.Transacao;
import com.saldium.saldium.security.user.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {
    List<Transacao> findAllByUsuario(Usuario usuario);

    Optional<Transacao> findByIdAndUsuario(Long id, Usuario usuario);

    void deleteByIdAndUsuario(Long id, Usuario usuario);

    boolean existsByCategoriaId(Long id);

    @Query("""
            SELECT COALESCE(SUM(t.valor), 0)
            FROM Transacao t
            WHERE t.usuario.id = :usuarioId
                AND t.tipoTransacao = 'RENDA'
                AND t.dataCriacao >= :dataInicio
                AND t.dataCriacao < :dataFim
""")
    BigDecimal totalRenda(Long usuarioId, OffsetDateTime dataInicio, OffsetDateTime dataFim);

    @Query("""
            SELECT COALESCE(SUM(t.valor), 0)
            FROM Transacao t
            WHERE t.usuario.id = :usuarioId
                AND t.tipoTransacao = 'DESPESA'
                AND t.dataCriacao >= :dataInicio
                AND t.dataCriacao < :dataFim
""")
    BigDecimal totalDespesas(Long usuarioId, OffsetDateTime dataInicio, OffsetDateTime dataFim);

    @Query("""
    SELECT new com.saldium.saldium.dto.relatorio.RelatorioCategoriaDTO(
        t.categoria.categoriaDoSistema,
        t.categoria.id,
        t.categoria.nome,
        t.categoria.tipo,
        SUM(t.valor)
    )
    FROM Transacao t
    WHERE t.usuario.id = :usuarioId
        AND t.dataCriacao >= :dataInicio
        AND t.dataCriacao < :dataFim
    GROUP BY t.categoria.categoriaDoSistema, t.categoria.id, t.categoria.nome, t.categoria.tipo
    ORDER BY SUM(t.valor) DESC
""")
    List<RelatorioCategoriaDTO> totalPorCategoria(Long usuarioId, OffsetDateTime dataInicio, OffsetDateTime dataFim);

    @Query(value = """
    SELECT
        CAST(EXTRACT(MONTH FROM t.data_transacao) AS INTEGER) AS mes,
        COALESCE(
            SUM(
                CASE
                    WHEN t.tipo_transacao = 'RENDA'
                    THEN t.valor
                    ELSE 0
                END
            ),
            0
        ) AS totalRenda,
        COALESCE(
            SUM(
                CASE
                    WHEN t.tipo_transacao = 'DESPESA'
                    THEN t.valor
                    ELSE 0
                END
            ),
            0
        ) AS totalDespesas
    FROM transacoes t
    WHERE t.usuario_id = :usuarioId
      AND EXTRACT(YEAR FROM t.data_transacao) = :ano
    GROUP BY EXTRACT(MONTH FROM t.data_transacao)
    ORDER BY mes
""", nativeQuery = true)
    List<ResumoMesDTO> buscarResumoAnual(
            @Param("usuarioId") Long usuarioId,
            @Param("ano") Integer ano
    );
}