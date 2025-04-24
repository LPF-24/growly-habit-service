package com.example.habit_service.service;

import com.example.habit_service.dto.HabitRequestDTO;
import com.example.habit_service.dto.HabitResponseDTO;
import com.example.habit_service.entity.Habit;
import com.example.habit_service.mapper.HabitModelMapper;
import com.example.habit_service.repository.HabitRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HabitService {
    private final HabitRepository habitRepository;
    private final HabitModelMapper habitModelMapper;

    public List<HabitResponseDTO> getAllHabitsByPersonId(long personId) {
        return habitRepository.findByPersonId(personId).stream().map(habitModelMapper::toResponseDTO).toList();
    }

    public HabitResponseDTO getHabitById(long habitId) {
        Habit foundHabit = habitRepository.findById(habitId)
                .orElseThrow(() -> new EntityNotFoundException("Habit with this id can't be found!"));

        return habitModelMapper.toResponseDTO(foundHabit);
    }

    public HabitResponseDTO createHabit(HabitRequestDTO dto) {
        Habit habit = habitModelMapper.fromRequestToHabit(dto);
        habitRepository.save(habit);
        return habitModelMapper.toResponseDTO(habit);
    }

    public void deleteHabit(long habitId) {
        habitRepository.deleteById(habitId);
    }

    public HabitResponseDTO updateHabit(long habitId, HabitRequestDTO dto) {
        Habit habitToUpdate = habitRepository.findById(habitId)
                .orElseThrow(() -> new EntityNotFoundException("Habit with id " + habitId + " not found"));

        if (dto.getName() != null && !dto.getName().isEmpty()) habitToUpdate.setName(dto.getName());
        if (dto.getDescription() != null && !dto.getDescription().isEmpty()) habitToUpdate.setDescription(dto.getDescription());
        if (dto.getActive() != null) habitToUpdate.setActive(dto.getActive());

        habitRepository.save(habitToUpdate);
        return habitModelMapper.toResponseDTO(habitToUpdate);
    }
}
