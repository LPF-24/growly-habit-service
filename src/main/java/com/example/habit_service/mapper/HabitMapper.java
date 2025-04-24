package com.example.habit_service.mapper;

import com.example.habit_service.dto.HabitRequestDTO;
import com.example.habit_service.dto.HabitResponseDTO;
import com.example.habit_service.entity.Habit;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class HabitMapper {
    private final ModelMapper modelMapper;

    public Habit toEntity(HabitRequestDTO dto) {
        Habit habit = new Habit();
        habit.setName(dto.getName());
        habit.setDescription(dto.getDescription());
        habit.setActive(dto.getActive() != null ? dto.getActive() : true);
        habit.setCreatedAt(LocalDate.now());
        habit.setPersonId(dto.getPersonId());
        return habit;
    }

    public HabitResponseDTO toResponseDTO(Habit habit) {
        return modelMapper.map(habit, HabitResponseDTO.class);
    }

    public Habit fromRequestToHabit(HabitRequestDTO dto) {
        return modelMapper.map(dto, Habit.class);
    }
}
