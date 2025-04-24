package com.example.habit_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "habit")
@NoArgsConstructor
@Getter
@Setter
public class Habit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;

    private boolean active = true;

    @Column(name = "person_id", nullable = false)
    private Long personId;
}
