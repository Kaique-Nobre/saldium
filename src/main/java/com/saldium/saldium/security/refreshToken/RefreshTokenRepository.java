package com.saldium.saldium.security.refreshToken;

import com.saldium.saldium.security.user.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByTokenAndRevogadoFalse(String token);
    List<RefreshToken> findAllByUsuario(Usuario usuario);

    @Modifying
    @Query("""
    DELETE FROM RefreshToken rt
    WHERE rt.revogado = true
       OR rt.expiraEm < :now
""")
    void deleteRevokedAndExpiredTokens(
            @Param("now") Instant now
    );
}
