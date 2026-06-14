package com.saldium.saldium.security.passwordResetToken;

import com.saldium.saldium.security.user.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetTokenService {
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public PasswordResetToken createPasswordResetToken(Usuario usuario) {
        PasswordResetToken token =
                PasswordResetToken.builder()
                        .token(UUID.randomUUID().toString())
                        .usuario(usuario)
                        .expiraEm(
                                Instant.now()
                                        .plus(1, ChronoUnit.HOURS)
                        )
                        .usado(false)
                        .build();
        return passwordResetTokenRepository.save(token);
    }
}
