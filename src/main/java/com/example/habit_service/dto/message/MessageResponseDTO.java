package com.example.habit_service.dto.message;

import com.example.habit_service.util.SwaggerConstants;
import io.swagger.v3.oas.annotations.media.Schema;

public class MessageResponseDTO {
    @Schema(description = SwaggerConstants.MESSAGE_DESC, example = SwaggerConstants.MESSAGE_EXAMPLE)
    private String message;

    public MessageResponseDTO(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

