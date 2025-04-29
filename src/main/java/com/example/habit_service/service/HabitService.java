package com.example.habit_service.service;

import com.example.habit_service.dto.HabitRequestDTO;
import com.example.habit_service.dto.HabitResponseDTO;
import com.example.habit_service.dto.HabitUpdateDTO;
import com.example.habit_service.entity.Habit;
import com.example.habit_service.mapper.HabitMapper;
import com.example.habit_service.repository.HabitRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.ws.rs.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Service
public class HabitService {
    private final HabitRepository habitRepository;
    private final HabitMapper habitMapper;

    public HabitService(HabitRepository habitRepository, HabitMapper habitMapper) {
        this.habitRepository = habitRepository;
        this.habitMapper = habitMapper;
    }

    @Transactional(readOnly = true)
    public List<HabitResponseDTO> getAllHabitsByPersonId(long personId) {
        return habitRepository.findByPersonId(personId).stream().map(habitMapper::toResponseDTO).toList();
    }

    @Transactional(readOnly = true)
    public HabitResponseDTO getHabitById(long habitId) {
        Habit foundHabit = habitRepository.findById(habitId)
                .orElseThrow(() -> new EntityNotFoundException("Habit with this id can't be found!"));

        return habitMapper.toResponseDTO(foundHabit);
    }

    @Transactional
    public HabitResponseDTO createHabit(HabitRequestDTO dto) {
        System.out.println("Mapping HabitRequestDTO to Habit");
        System.out.println("DTO name: " + dto.getName());
        Habit habit = habitMapper.toEntity(dto);
        System.out.println("habitRequestDTO successfully mapped to Habit in service.");
        System.out.println(habit.getName());
        habit.setCreatedAt(LocalDate.now());
        habitRepository.save(habit);
        System.out.println("habit saved successfully in service");
        return habitMapper.toResponseDTO(habit);
    }

    @Transactional
    public void deleteHabit(long habitId) {
        habitRepository.deleteById(habitId);
    }

    @Transactional
    public HabitResponseDTO updateHabit(long habitId, HabitUpdateDTO dto) {
        Habit habitToUpdate = habitRepository.findById(habitId)
                .orElseThrow(() -> new EntityNotFoundException("Habit with id " + habitId + " not found"));

        boolean allFieldsNull = Stream.of(
                dto.getName(),
                dto.getDescription(),
                dto.getActive(),
                dto.getPersonId()
        ).allMatch(Objects::isNull);

        if (allFieldsNull) {
            throw new BadRequestException("Nothing to update");
        }

        habitMapper.updateHabitFromDtoWithFixedFields(dto, habitToUpdate);
        return habitMapper.toResponseDTO(habitRepository.save(habitToUpdate));
    }
}
