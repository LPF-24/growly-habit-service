package com.example.habit_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class HabitRequestDTO {
    @NotBlank
    private String name;

    private String description;

    private Boolean active;

    @NotNull
    private Long personId;
}
