package com.saldium.saldium.security.refreshToken;

import com.saldium.saldium.security.user.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private boolean revogado;

    @Column(nullable = false)
    private Instant expiraEm;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}
