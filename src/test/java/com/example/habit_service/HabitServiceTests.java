package com.example.habit_service;

import com.example.habit_service.dto.HabitRequestDTO;
import com.example.habit_service.dto.HabitResponseDTO;
import com.example.habit_service.dto.HabitUpdateDTO;
import com.example.habit_service.entity.Habit;
import com.example.habit_service.mapper.HabitMapper;
import com.example.habit_service.mapper.HabitMapperImpl;
import com.example.habit_service.repository.HabitRepository;
import com.example.habit_service.service.HabitService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class HabitServiceTests {
    @Mock
    private HabitRepository habitRepository;

    private final HabitMapper habitMapper = new HabitMapperImpl();

    private HabitService habitService;

    @BeforeEach
    void setUp() {
        habitService = new HabitService(habitRepository, habitMapper);
    }

    @Test
    void shouldReturnMappedHabitResponseDTOs_whenPersonIdExists() {
        // Arrange
        Habit habit1 = new Habit();
        habit1.setId(1L);
        habit1.setName("Drink water");
        habit1.setActive(true);
        habit1.setCreatedAt(LocalDate.of(2024, 1, 1));
        habit1.setPersonId(100L);

        Habit habit2 = new Habit();
        habit2.setId(2L);
        habit2.setName("Do sport");
        habit2.setActive(false);
        habit2.setCreatedAt(LocalDate.of(2024, 2, 2));
        habit2.setPersonId(100L);

        when(habitRepository.findByPersonId(100L)).thenReturn(List.of(habit1, habit2));

        // Act
        List<HabitResponseDTO> result = habitService.getAllHabitsByPersonId(100L);

        // Assert
        verify(habitRepository).findByPersonId(100L);
        assertEquals(2, result.size()); // почему 2?

        HabitResponseDTO dto1 = result.get(0);
        assertEquals("Drink water", dto1.getName());
        assertEquals(100L, dto1.getPersonId());
        assertTrue(dto1.isActive());
        assertEquals(LocalDate.of(2024, 1, 1), dto1.getCreatedAt());

        HabitResponseDTO dto2 = result.get(1);
        assertEquals("Do sport", dto2.getName());
        assertEquals(100L, dto2.getPersonId());
        assertFalse(dto2.isActive());
        assertEquals(LocalDate.of(2024, 2, 2), dto2.getCreatedAt());
    }

    @Test
    void shouldGetHabitByIdAndReturnResponseDTO_whenHabitIdGiven() {
        HabitRequestDTO requestDTO = createSampleRequestDTO();

        HabitResponseDTO result = habitService.createHabit(1L, requestDTO);

        verify(habitRepository).save(any(Habit.class));
        assertEquals(1L, result.getPersonId());
        assertTrue(result.isActive());
        assertEquals(LocalDate.of(2025, 7, 5), result.getCreatedAt());
    }

    @Nested
    class getHabitByHabitIdTests {

        @Test
        void shouldGetHabitByIdAndReturnResponseDTO_whenHabitIdGiven() {
            Habit habit = createSampleHabit();

            when(habitRepository.findById(1L)).thenReturn(Optional.of(habit));

            HabitResponseDTO result = habitService.getHabitById(1L);

            verify(habitRepository).findById(1L);
            assertEquals(LocalDate.of(2024, 2, 2), result.getCreatedAt());
            // Это уже доказательство, что toResponseDTO(...) отработал правильно, потому что DTO пришёл не из воздуха.
            assertEquals("Drink water", result.getName());
            assertTrue(result.isActive());
        }

        @Test
        void shouldThrowsException_whenHabitNotFound() {
            when(habitRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> habitService.getHabitById(1L));
        }
    }

    @Test
    void shouldDeleteHabitByIdAndReturnResponseDTO() {
        habitRepository.deleteById(1L);

        verify(habitRepository).deleteById(1L);
    }

    @Nested
    class updateHabitTests {

        @Test
        void shouldUpdateHabitAndReturnResponseDTO_whenHabitIdGiven () {
            Habit habit = createSampleHabit();
            HabitUpdateDTO updateDTO = new HabitUpdateDTO();
            updateDTO.setName("Ride a bike");
            updateDTO.setActive(false);

            when(habitRepository.findById(1L)).thenReturn(Optional.of(habit));
            when(habitRepository.save(habit)).thenReturn(habit); // нужно, т.к. сервис вызывает save(...)

            HabitResponseDTO result = habitService.updateHabit(1L, updateDTO);

            verify(habitRepository).findById(1L);
            verify(habitRepository).save(habit);
            assertEquals("Ride a bike", result.getName());
            assertFalse(result.isActive());
        }

        @Test
        void shouldThrowsException_whenHabitNotFound() {
            HabitUpdateDTO updateDTO = new HabitUpdateDTO();
            updateDTO.setName("Ride a bike");
            updateDTO.setActive(false);

            when(habitRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> habitService.updateHabit(1L, updateDTO));
        }

        @Test
        void shouldThrowsException_whenAllFieldsNull() {
            Habit habit = createSampleHabit();
            HabitUpdateDTO updateDTO = new HabitUpdateDTO();

            when(habitRepository.findById(1L)).thenReturn(Optional.of(habit));

            assertThrows(ResponseStatusException.class, () -> habitService.updateHabit(1L, updateDTO));
        }
    }

    private static Habit createSampleHabit() {
        Habit habit = new Habit();
        habit.setPersonId(1L);
        habit.setCreatedAt(LocalDate.now());
        habit.setActive(true);
        habit.setName("Drink water");
        habit.setCreatedAt(LocalDate.of(2024, 2, 2));
        return habit;
    }

    private static HabitResponseDTO createSampleResponseDTO() {
        HabitResponseDTO responseDTO = new HabitResponseDTO();
        responseDTO.setPersonId(1L);
        responseDTO.setCreatedAt(LocalDate.now());
        responseDTO.setActive(true);
        responseDTO.setName("Test");
        return responseDTO;
    }

    private static HabitRequestDTO createSampleRequestDTO() {
        HabitRequestDTO requestDTO = new HabitRequestDTO();
        requestDTO.setActive(true);
        requestDTO.setName("Test");
        requestDTO.setDescription("Test description");
        return requestDTO;
    }

    /*@Nested
    class getAllHabitsTests {

        @Test
        void shouldReturnAllHabits_whenGivenPersonID() {
            Habit habit = createSampleHabit();
            HabitResponseDTO responseDTO = createSampleResponseDTO();

            when(habitRepository.findByPersonId(1L)).thenReturn(List.of(habit));
            when(habitMapper.toResponseDTO(habit)).thenReturn(responseDTO);

            List<HabitResponseDTO> result = habitService.getAllHabitsByPersonId(1L);

            verify(habitMapper).toResponseDTO(habit);
            assertEquals(1, result.size());
            assertEquals("Test", result.get(0).getName());
        }
    }

    @Nested
    class getHabitByHabitIdTests {

        @Test
        void shouldGetHabitByIdAndReturnResponseDTO_whenHabitIdGiven() {
            Habit habit = createSampleHabit();
            HabitResponseDTO responseDTO = createSampleResponseDTO();

            when(habitRepository.findById(1L)).thenReturn(Optional.of(habit));
            when(habitMapper.toResponseDTO(habit)).thenReturn(responseDTO);

            HabitResponseDTO result = habitService.getHabitById(1L);

            verify(habitMapper).toResponseDTO(habit);
            assertEquals("Test", result.getName());
            assertTrue(result.isActive());
        }
    }

    @Test
    void shouldSaveHabitAndReturnResponseDTO() {
        HabitRequestDTO requestDTO = createSampleRequestDTO();
        Habit habit = createSampleHabit();

        Habit savedHabit = createSampleHabit();
        HabitResponseDTO responseDTO = createSampleResponseDTO();

        when(habitMapper.toEntity(requestDTO)).thenReturn(habit);
        when(habitRepository.save(habit)).thenReturn(savedHabit);
        when(habitMapper.toResponseDTO(savedHabit)).thenReturn(responseDTO);

        HabitResponseDTO result = habitService.createHabit(1L, requestDTO);
        verify(habitMapper).toEntity(requestDTO);
        verify(habitRepository).save(habit);
        assertEquals("Test", result.getName());
        assertEquals(1L, result.getPersonId());
    }

    */
}
