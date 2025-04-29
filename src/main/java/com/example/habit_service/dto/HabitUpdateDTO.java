package com.example.habit_service.dto;

import com.example.habit_service.util.SwaggerConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public class HabitUpdateDTO {
    @Schema(description = SwaggerConstants.NAME_DESC, example = SwaggerConstants.NAME_EXAMPLE)
    @Size(min = 2, max = 255, message = "Product name must be between 2 and 255 characters")
    private String name;

    @Schema(description = SwaggerConstants.DESCRIPTION_DESC, example = SwaggerConstants.DESCRIPTION_EXAMPLE)
    @Size(max = 255, message = "Description must be between 0 and 255 characters")
    private String description;

    @Schema(description = SwaggerConstants.ACTIVE_DESC, example = SwaggerConstants.ACTIVE_EXAMPLE)
    private Boolean active;

    @Schema(description = SwaggerConstants.PERSON_ID_DESC, example = SwaggerConstants.PERSON_ID_EXAMPLE)
    private Long personId;

    public HabitUpdateDTO() {
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }
}
