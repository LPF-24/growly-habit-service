package com.example.habit_service.service;

import com.example.habit_service.dto.HabitRequestDTO;
import com.example.habit_service.dto.HabitResponseDTO;
import com.example.habit_service.dto.HabitUpdateDTO;
import com.example.habit_service.entity.Habit;
import com.example.habit_service.mapper.HabitMapper;
import com.example.habit_service.repository.HabitRepository;
import com.example.habit_service.security.JWTFilter;
import jakarta.persistence.EntityNotFoundException;
import jakarta.ws.rs.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Service
public class HabitService {
    private final HabitRepository habitRepository;
    private final HabitMapper habitMapper;
    private final Logger logger = LoggerFactory.getLogger(HabitService.class);

    public HabitService(HabitRepository habitRepository, HabitMapper habitMapper) {
        this.habitRepository = habitRepository;
        this.habitMapper = habitMapper;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<HabitResponseDTO> getAllHabitsByPersonId(long personId) {
        logger.info("getAllHabitsByPersonId started");
        return habitRepository.findByPersonId(personId).stream().map(habitMapper::toResponseDTO).toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@habitSecurity.isOwner(#habitId)")
    public HabitResponseDTO getHabitById(long habitId) {
        Habit foundHabit = habitRepository.findById(habitId)
                .orElseThrow(() -> new EntityNotFoundException("Habit with this id can't be found!"));

        return habitMapper.toResponseDTO(foundHabit);
    }

    @Transactional
    @PreAuthorize("isAuthenticated()")
    public HabitResponseDTO createHabit(Long id, HabitRequestDTO dto) {
        logger.info("Mapping HabitRequestDTO to Habit");
        logger.debug("DTO name: {}", dto.getName());
        Habit habit = habitMapper.toEntity(dto);
        logger.debug("habitRequestDTO successfully mapped to Habit in service.");
        logger.info(habit.getName());
        habit.setCreatedAt(LocalDate.now());
        habit.setPersonId(id);
        habitRepository.save(habit);
       logger.info("habit saved successfully in service");
        return habitMapper.toResponseDTO(habit);
    }

    @Transactional
    @PreAuthorize("@habitSecurity.isOwner(#habitId)")
    public void deleteHabit(long habitId) {
        habitRepository.deleteById(habitId);
    }

    @Transactional
    @PreAuthorize("@habitSecurity.isOwner(#habitId)")
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nothing to update");
        }

        habitMapper.updateHabitFromDtoWithFixedFields(dto, habitToUpdate);
        return habitMapper.toResponseDTO(habitRepository.save(habitToUpdate));
    }
}
