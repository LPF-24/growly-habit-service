package com.example.habit_service.exception;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Structure for error responses.")
public class ErrorResponseDTO {

    @Schema(description = "HTTP status code", example = "404")
    private int status;

    @Schema(description = "Error description", example = "Not Found")
    private String error;

    @Schema(description = "Detailed error message", example = "Habit with id 5 not found.")
    private String message;

    @Schema(description = "The request path that caused the error", example = "/habits/5")
    private String path;

    // Constructors, getters, setters
    public ErrorResponseDTO() {}

    public ErrorResponseDTO(int status, String error, String message, String path) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
}

