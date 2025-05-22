package com.example.habit_service.dto;

import com.example.habit_service.util.SwaggerConstants;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class HabitRequestDTO {
    @Schema(description = SwaggerConstants.NAME_DESC, example = SwaggerConstants.NAME_EXAMPLE)
    @NotNull(message = "The name of the habit should not be empty")
    @Size(min = 2, max = 255, message = "Product name must be between 2 and 255 characters")
    private String name;

    @Schema(description = SwaggerConstants.DESCRIPTION_DESC, example = SwaggerConstants.DESCRIPTION_EXAMPLE)
    @Size(max = 255, message = "Description must be between 0 and 255 characters")
    private String description;

    @Schema(description = SwaggerConstants.ACTIVE_DESC, example = SwaggerConstants.ACTIVE_EXAMPLE)
    @NotNull(message = "Active status can't be empty")
    private Boolean active;

    public HabitRequestDTO() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
