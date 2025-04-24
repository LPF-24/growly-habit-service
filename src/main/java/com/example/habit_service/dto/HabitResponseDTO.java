package com.example.habit_service.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@NoArgsConstructor
@Getter
@Setter
public class HabitResponseDTO {
    private Long id;
    private String name;
    private String description;
    private LocalDate createdAt;
    private boolean active;
    private Long personId;
}
