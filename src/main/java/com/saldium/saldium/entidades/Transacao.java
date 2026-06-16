package com.saldium.saldium.entidades;

import com.saldium.saldium.security.user.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "transacoes")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Transacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String descricao;

    @Column(nullable = false)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    private TipoTransacao tipoTransacao;

    @JoinColumn(name = "usuario_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Usuario usuario;

    @JoinColumn(name = "categoria_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    Categoria categoria;

    @Column(nullable = false)
    private LocalDate dataTransacao;

    private OffsetDateTime dataCriacao;
}
