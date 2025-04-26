package com.example.habit_service.dto;

import com.example.habit_service.util.SwaggerConstants;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public class HabitResponseDTO {
    @Schema(description = SwaggerConstants.ID_DESC, example = SwaggerConstants.ID_EXAMPLE)
    private Long id;

    @Schema(description = SwaggerConstants.NAME_DESC, example = SwaggerConstants.NAME_EXAMPLE)
    private String name;

    @Schema(description = SwaggerConstants.DESCRIPTION_DESC, example = SwaggerConstants.DESCRIPTION_EXAMPLE)
    private String description;

    @Schema(description = SwaggerConstants.CREATED_AT_DESC, example = SwaggerConstants.CREATED_AT_EXAMPLE)
    private LocalDate createdAt;

    @Schema(description = SwaggerConstants.ACTIVE_DESC, example = SwaggerConstants.ACTIVE_EXAMPLE)
    private boolean active;

    @Schema(description = SwaggerConstants.PERSON_ID_DESC, example = SwaggerConstants.PERSON_ID_EXAMPLE)
    private Long personId;

    public HabitResponseDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }
}
