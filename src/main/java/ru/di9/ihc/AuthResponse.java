package ru.di9.ihc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AuthResponse(
        Alert alert
) {
    public record Alert(
            String type,
            String message
    ) {
    }
}
