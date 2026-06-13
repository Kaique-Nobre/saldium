package com.saldium.saldium.dto.email;

import jakarta.validation.constraints.Email;

public record ResendVerificationEmailRequestDTO(
        @Email
        String email
) {
}
