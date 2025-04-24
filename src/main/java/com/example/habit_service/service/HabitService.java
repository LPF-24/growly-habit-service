package com.example.habit_service.service;

import com.example.habit_service.dto.HabitRequestDTO;
import com.example.habit_service.dto.HabitResponseDTO;
import com.example.habit_service.entity.Habit;
import com.example.habit_service.mapper.HabitMapper;
import com.example.habit_service.repository.HabitRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HabitService {
    private final HabitRepository habitRepository;
    private final HabitMapper habitMapper;

    public List<HabitResponseDTO> getAllHabitsByPersonId(long personId) {
        return habitRepository.findByPersonId(personId).stream().map(habitMapper::toResponseDTO).toList();
    }

    public HabitResponseDTO getHabitById(long habitId) {
        Habit foundHabit = habitRepository.findById(habitId)
                .orElseThrow(() -> new EntityNotFoundException("Habit with this id can't be found!"));

        return habitMapper.toResponseDTO(foundHabit);
    }

    public HabitResponseDTO createHabit(HabitRequestDTO dto) {
        Habit habit = habitMapper.fromRequestToHabit(dto);
        habitRepository.save(habit);
        return habitMapper.toResponseDTO(habit);
    }

    public void deleteHabit(long habitId) {
        habitRepository.deleteById(habitId);
    }
}
