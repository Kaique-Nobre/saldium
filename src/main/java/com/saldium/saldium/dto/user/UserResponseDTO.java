package com.saldium.saldium.dto.user;

import java.time.LocalDate;

public record UserResponseDTO(
        String nome,
        String email,
        LocalDate createdAt
) {
}
