package com.saldium.saldium.security.token;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class TokenCleanupJob {

    private final RefreshTokenRepository repository;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanup() {

        repository.deleteRevokedAndExpiredTokens(
                Instant.now()
        );
    }
}
