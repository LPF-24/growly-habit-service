package com.example.habit_service.mapper;

import com.example.habit_service.dto.HabitRequestDTO;
import com.example.habit_service.dto.HabitResponseDTO;
import com.example.habit_service.entity.Habit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface HabitMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Habit toEntity(HabitRequestDTO dto);

    HabitResponseDTO toResponseDTO(Habit habit);

    List<HabitResponseDTO> toResponseDTOList(List<Habit> habits);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updateHabitFromDtoWithFixedFields(HabitRequestDTO dto, @MappingTarget Habit habit);
}
