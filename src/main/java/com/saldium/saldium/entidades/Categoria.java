package com.saldium.saldium.entidades;

import com.saldium.saldium.security.user.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "categorias", uniqueConstraints = {@UniqueConstraint(columnNames = {"nome", "usuario_id"})})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Enumerated(EnumType.STRING)
    private TipoTransacao tipo;

    @JoinColumn(name = "usuario_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Usuario usuario;

    private boolean categoriaDoSistema;

    public Categoria(Long id, String nome, TipoTransacao tipo, boolean categoriaDoSistema) {
        this.id = id;
        this.nome = nome;
        this.tipo = tipo;
        this.categoriaDoSistema = categoriaDoSistema;
    }
}
