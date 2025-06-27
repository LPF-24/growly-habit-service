package com.example.habit_service.controller;

import com.example.habit_service.dto.HabitRequestDTO;
import com.example.habit_service.dto.HabitResponseDTO;
import com.example.habit_service.dto.HabitUpdateDTO;
import com.example.habit_service.dto.message.MessageResponseDTO;
import com.example.habit_service.exception.ErrorResponseDTO;
import com.example.habit_service.exception.ErrorUtil;
import com.example.habit_service.security.PersonDetails;
import com.example.habit_service.service.HabitEventPublisher;
import com.example.habit_service.service.HabitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Habit", description = "Endpoints for habit management.")
@RestController
@RequestMapping("/")
public class HabitController {
    private final HabitEventPublisher publisher;
    private final HabitService habitService;
    private final Logger logger = LoggerFactory.getLogger(HabitController.class);

    public HabitController(HabitEventPublisher publisher, HabitService habitService) {
        this.publisher = publisher;
        this.habitService = habitService;
    }

    @Operation(summary = "Get all habits", description = "Returns all habits for a specific person.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of habits",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = HabitResponseDTO.class)))),
                    @ApiResponse(responseCode = "400", description = "Missing required parameters.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDTO.class),
                                    examples = @ExampleObject(
                                            name = "HabitNotFound",
                                            summary = "Example of 400 Missing Required Parameters",
                                            value = "{\n" +
                                                    "  \"status\": 400,\n" +
                                                    "  \"error\": \"Missing Required Parameters\",\n" +
                                                    "  \"message\": \"Missing required parameters.\",\n" +
                                                    "  \"path\": \"/habits\"\n" +
                                                    "}"
                                    ))),
                    @ApiResponse(responseCode = "500", description = "Internal server error.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDTO.class),
                                    examples = @ExampleObject(
                                            name = "Internal server error",
                                            summary = "Example of 500 Internal Server Error",
                                            value = "{\n" +
                                                    "  \"status\": 500,\n" +
                                                    "  \"error\": \"Internal Server Error\",\n" +
                                                    "  \"path\": \"/habits\"\n" +
                                                    "}"
                                    )))
            })
    @GetMapping("/all-habits")
    public ResponseEntity<List<HabitResponseDTO>> getAllHabits(@AuthenticationPrincipal PersonDetails user) {
        logger.info("Authenticated user: {}", (user != null ? user.getUsername() : "null"));
        Long personId = user.getId();
        logger.info("User id: {}", personId);

        List<HabitResponseDTO> habits = habitService.getAllHabitsByPersonId(personId);
        logger.info("Habits successfully received");
        return ResponseEntity.ok(habits);
    }

    @Operation(summary = "Get a habit", description = "Returns a habit by its ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Habit found",
                            content = @Content(schema = @Schema(implementation = HabitResponseDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Habit not found.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDTO.class),
                                    examples = @ExampleObject(
                                            name = "HabitNotFound",
                                            summary = "Example of 404 Not Found",
                                            value = "{\n" +
                                                    "  \"status\": 404,\n" +
                                                    "  \"error\": \"Not Found\",\n" +
                                                    "  \"message\": \"Habit with id 5 not found.\",\n" +
                                                    "  \"path\": \"/habits/5\"\n" +
                                                    "}"
                                    ))),
                    @ApiResponse(responseCode = "500", description = "Internal server error.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDTO.class),
                                    examples = @ExampleObject(
                                            name = "Internal server error",
                                            summary = "Example of 500 Internal Server Error",
                                            value = "{\n" +
                                                    "  \"status\": 500,\n" +
                                                    "  \"error\": \"Internal Server Error\",\n" +
                                                    "  \"path\": \"/habits/5\"\n" +
                                                    "}"
                                    )))
            })
    @GetMapping("/{id}")
    public ResponseEntity<HabitResponseDTO> getHabit(@PathVariable Long id) {
        return ResponseEntity.ok(habitService.getHabitById(id));
    }

    @Operation(summary = "Create a habit.", description = "Adds a new habit.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "CreateHabitRequest",
                                    summary = "Example habit creation request",
                                    value = "{\n" +
                                            "  \"name\": \"Drink water\",\n" +
                                            "  \"description\": \"Drink 2L of water every day\",\n" +
                                            "  \"active\": true,\n" +
                                            "  \"personId\": 45\n" +
                                            "}"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Habit successfully created."),
                    @ApiResponse(responseCode = "500", description = "Internal server error.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDTO.class),
                                    examples = @ExampleObject(
                                            name = "Internal server error",
                                            summary = "Example of 500 Internal Server Error",
                                            value = "{\n" +
                                                    "  \"status\": 500,\n" +
                                                    "  \"error\": \"Internal Server Error\",\n" +
                                                    "  \"path\": \"/habits\"\n" +
                                                    "}"
                                    )))
            }
    )
    @PostMapping("/create-habit")
    public ResponseEntity<HabitResponseDTO> newHabit(@org.springframework.web.bind.annotation.RequestBody
                                                         @Valid HabitRequestDTO dto, BindingResult bindingResult,
                                                     @AuthenticationPrincipal PersonDetails user) {
        // Логирование для проверки, что приходит в DTO
        logger.info("Received DTO in controller: {}", dto.getName());

        if (bindingResult.hasErrors()) {
            // Логируем все ошибки валидации
            bindingResult.getAllErrors().forEach(error -> {
                logger.error("Validation error: {}", error.getDefaultMessage());
            });
            // Если есть ошибки, выбрасываем их
            ErrorUtil.throwIfHasErrors(bindingResult);
        }

        // Если валидация прошла успешно, передаем DTO в сервис
        HabitResponseDTO response = habitService.createHabit(user.getId(), dto);

        // Возвращаем успешно созданный объект
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete a habit", description = "Deletes a habit by ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Habit successfully deleted.",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "Successfully",
                                            summary = "Example of 200 Successfully",
                                            value = "{\n" +
                                                    "  \"message\": \"Habit with id 5 successfully removed.\"\n" +
                                                    "}",
                                            externalValue = "", // <--- оставить пустым
                                            description = "Successful delete response",
                                            ref = "" // <--- оставить пустым
                                    )

                            )),
                    @ApiResponse(responseCode = "403", description = "Forbidden.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDTO.class),
                                    examples = @ExampleObject(
                                            name = "Forbidden",
                                            summary = "Example of 403 Forbidden",
                                            value = "{\n" +
                                                    "  \"status\": 403,\n" +
                                                    "  \"error\": \"Forbidden\",\n" +
                                                    "  \"message\": \"Forbidden\",\n" +
                                                    "  \"path\": \"/habits/5\"\n" +
                                                    "}"
                                    ))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDTO.class),
                                    examples = @ExampleObject(
                                            name = "Unauthorized",
                                            summary = "Example of 401 Unauthorized",
                                            value = "{\n" +
                                                    "  \"status\": 401,\n" +
                                                    "  \"error\": \"Unauthorized\",\n" +
                                                    "  \"message\": \"Unauthorized.\",\n" +
                                                    "  \"path\": \"/habits/5\"\n" +
                                                    "}"
                                    ))),
                    @ApiResponse(responseCode = "500", description = "Internal server error.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDTO.class),
                                    examples = @ExampleObject(
                                            name = "Internal server error",
                                            summary = "Example of 500 Internal Server Error",
                                            value = "{\n" +
                                                    "  \"status\": 500,\n" +
                                                    "  \"error\": \"Internal Server Error\",\n" +
                                                    "  \"path\": \"/habits/5\"\n" +
                                                    "}"
                                    ))),
                    @ApiResponse(responseCode = "503", description = "Service unavailable.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDTO.class),
                                    examples = @ExampleObject(
                                            name = "Service unavailable",
                                            summary = "Example of 503 Service unavailable.",
                                            value = "{\n" +
                                                    "  \"status\": 503,\n" +
                                                    "  \"error\": \"Service unavailable\",\n" +
                                                    "  \"path\": \"/habits/5\"\n" +
                                                    "}"
                                    )))
            })
    @DeleteMapping("delete/{id}")
    public ResponseEntity<MessageResponseDTO> deleteHabit(@PathVariable Long id) {
        habitService.deleteHabit(id);
        publisher.habitDeleted(id);
        return ResponseEntity.ok(new MessageResponseDTO("Habit with id " + id + " successfully removed."));
    }

    @Operation(summary = "Update a habit", description = "Updates a habit partially by ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Habit successfully updated."),
            @ApiResponse(responseCode = "400", description = "Validation failed.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "ValidationError",
                                    summary = "Example of 400 Validation Error",
                                    value = "{\n" +
                                            "  \"status\": 400,\n" +
                                            "  \"error\": \"Bad Request\",\n" +
                                            "  \"message\": \"Validation failed: name must not be blank\",\n" +
                                            "  \"path\": \"/habits/5\"\n" +
                                            "}"
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Habit not found.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "HabitNotFound",
                                    summary = "Example of 404 Habit Not Found",
                                    value = """
                                            {
                                              "status": 404,
                                              "error": "Not Found",
                                              "message": "Habit with id 5 not found.",
                                              "path": "/habits/5"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "InternalServerError",
                                    summary = "Example of 500 Internal Server Error",
                                    value = "{\n" +
                                            "  \"status\": 500,\n" +
                                            "  \"error\": \"Internal Server Error\",\n" +
                                            "  \"message\": \"Unexpected error occurred.\",\n" +
                                            "  \"path\": \"/habits/5\"\n" +
                                            "}"
                            )
                    )
            )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "UpdateHabitRequest",
                            summary = "Example habit update request",
                            value = "{ \"description\": \"Drink 3L of water per day\", \"active\": false }"
                    )
            )
    )
    @PatchMapping("update/{id}")
    public ResponseEntity<HabitResponseDTO> updateHabit(@PathVariable Long id,
                                                        @org.springframework.web.bind.annotation.RequestBody
                                                        @Valid HabitUpdateDTO dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            ErrorUtil.throwIfHasErrors(bindingResult);

        publisher.habitUpdated(id);
        return ResponseEntity.ok(habitService.updateHabit(id, dto));
    }
}
