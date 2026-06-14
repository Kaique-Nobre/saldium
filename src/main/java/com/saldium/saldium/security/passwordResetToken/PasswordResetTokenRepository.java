package com.saldium.saldium.security.passwordResetToken;

import com.saldium.saldium.security.user.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    List<PasswordResetToken> findAllByUsuario(Usuario usuario);
}
