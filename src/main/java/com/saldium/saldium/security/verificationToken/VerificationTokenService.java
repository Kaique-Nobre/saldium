package com.saldium.saldium.security.verificationToken;

import com.saldium.saldium.exceptions.auth.TokenInvalidoException;
import com.saldium.saldium.security.user.UserRepository;
import com.saldium.saldium.security.user.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VerificationTokenService {
    private final VerificationTokenRepository verificationTokenRepository;
    private final UserRepository userRepository;

    @Transactional(rollbackFor = Exception.class)
    public VerificationToken createVerificationToken(Usuario usuario) {
        VerificationToken token = new VerificationToken();

        token.setToken(UUID.randomUUID().toString());
        token.setUsuario(usuario);
        token.setUsado(false);
        token.setExpiraEm(Instant.now().plus(24, ChronoUnit.HOURS));

        return verificationTokenRepository.save(token);
    }

    @Transactional(rollbackFor = Exception.class)
    public void verifyEmail(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenInvalidoException("Token inválido ou expirado"));

        if (verificationToken.isUsado()) {
            throw new TokenInvalidoException("Token já utilizado");
        }

        if (verificationToken.getExpiraEm().isBefore(Instant.now())) {
            throw new TokenInvalidoException("Token expirado");
        }

        Usuario usuario = verificationToken.getUsuario();

        usuario.setEmailVerificado(true);
        verificationToken.setUsado(true);

        userRepository.save(usuario);
        verificationTokenRepository.save(verificationToken);
    }
}
