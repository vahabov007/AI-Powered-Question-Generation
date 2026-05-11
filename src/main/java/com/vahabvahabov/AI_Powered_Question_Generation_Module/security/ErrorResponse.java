package com.vahabvahabov.AI_Powered_Question_Generation_Module.security;

import java.time.Instant;

public record ErrorResponse(
        int status,
        String message,
        String error,
        String path,
        Instant timestamp
) {
}
