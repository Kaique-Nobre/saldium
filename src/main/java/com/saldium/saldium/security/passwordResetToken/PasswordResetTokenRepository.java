package com.saldium.saldium.security.passwordResetToken;

import com.saldium.saldium.security.user.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);

    List<PasswordResetToken> findAllByUsuario(Usuario usuario);

    @Modifying
    @Transactional
    @Query("""
    DELETE
    FROM PasswordResetToken pt
    WHERE pt.usuario.id = :usuarioId
""")
    void deleteAllFromUsuario(@Param("usuarioId") Long usuarioId);
}
