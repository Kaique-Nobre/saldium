package com.saldium.saldium.exceptions;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ErrorResponse(
        String title,
        String message,
        int status,
        LocalDateTime timestamp
) {
}
