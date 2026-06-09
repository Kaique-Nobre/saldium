package com.saldium.saldium.security.token;

import com.saldium.saldium.security.user.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByTokenAndRevogadoFalse(String token);
    List<RefreshToken> findAllByUsuario(Usuario usuario);
}
