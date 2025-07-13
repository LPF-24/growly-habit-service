package com.example.habit_service.controller;

import com.example.habit_service.HabitServiceApplication;
import com.example.habit_service.dto.HabitRequestDTO;
import com.example.habit_service.entity.Habit;
import com.example.habit_service.repository.HabitRepository;
import com.example.habit_service.security.HabitSecurity;
import com.example.habit_service.security.JWTUtil;
import com.example.habit_service.security.Person;
import com.example.habit_service.security.PersonDetails;
import com.example.habit_service.service.HabitEventPublisher;
import com.example.habit_service.service.HabitService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest(classes = {HabitServiceApplication.class, TestKafkaConfig.class})
public class HabitControllerTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private HabitRepository habitRepository;
    @Autowired private HabitService habitService;
    @Autowired private JWTUtil jwtUtil;

    @MockBean
    private HabitSecurity habitSecurity;

    @MockBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private HabitEventPublisher habitEventPublisher;

    @AfterEach
    void clearDatabase() {
        habitRepository.deleteAll();
    }


    @Nested
    class allHabitsTests {
        private String token;
        private PersonDetails personDetails;

        @BeforeEach
        void setUp() {
            Person person = createSamplePerson(123L, "testuser", "ROLE_USER");
            personDetails = new PersonDetails(person);
            token = jwtUtil.generateAccessToken(person.getId(), person.getUsername(), "ROLE_USER");
            System.out.println("Token in test: " + token);

            Habit habit1 = createSampleHabit(123L, "Test Habit", true, "Test description");
            habitRepository.save(habit1);
        }

        @Test
        void allHabits_success() throws Exception {
            Authentication auth = new UsernamePasswordAuthenticationToken(personDetails, null, personDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);

            mockMvc.perform(get("/all-habits")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].name").value("Test Habit"))
                    .andExpect(jsonPath("$[0].active").value(true));
        }

        @Test
        void allHabits_shouldReturn403_whenTokenIsMissing() throws Exception {
            SecurityContextHolder.clearContext();

            mockMvc.perform(get("/all-habits"))
                    .andExpect(status().isForbidden());
        }

        @Test
        void allHabits_shouldReturnEmptyList_whenUserHasNoHabits() throws Exception {
            // Создаём пользователя без привычек
            Person person = createSamplePerson(999L, "userWithoutHabits", "ROLE_USER");
            String tokenWithoutHabits = jwtUtil.generateAccessToken(person.getId(), person.getUsername(), person.getRole());

            // Устанавливаем Authentication в контекст (если требуется PreAuthorize)
            PersonDetails personDetails = new PersonDetails(person);
            Authentication auth = new UsernamePasswordAuthenticationToken(personDetails, null, personDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);

            mockMvc.perform(get("/all-habits")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenWithoutHabits))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0)); // проверяем, что список пустой
        }

        @Test
        void allHabits_shouldReturnEmptyList_whenTokenPersonIdDoesNotMatchAnyHabit() throws Exception {
            // В БД создаётся привычка, но у другого пользователя
            Habit habit = createSampleHabit(555L, "Alien Habit", true, "not for this user");
            habitRepository.save(habit);

            // Создаём токен с другим ID — пользователь не имеет привычек
            Long tokenPersonId = 999L;
            Person person = createSamplePerson(tokenPersonId, "otherUser", "ROLE_USER");
            String token = jwtUtil.generateAccessToken(person.getId(), person.getUsername(), person.getRole());

            PersonDetails personDetails = new PersonDetails(person);
            Authentication auth = new UsernamePasswordAuthenticationToken(personDetails, null, personDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);

            mockMvc.perform(get("/all-habits")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    @Nested
    class getHabitTests {
        @Test
        void getHabitById_shouldReturn200_whenHabitExistsAndUserIsOwner() throws Exception {
            Long personId = 123L;
            Person person = createSamplePerson(personId, "testuser", "ROLE_USER");
            PersonDetails personDetails = new PersonDetails(person);
            String token = jwtUtil.generateAccessToken(person.getId(), person.getUsername(), person.getRole());

            Habit habit = createSampleHabit(personId, "Reading", true, "Read every morning");
            habitRepository.save(habit);

            Authentication auth = new UsernamePasswordAuthenticationToken(personDetails, null, personDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);

            when(habitSecurity.isOwner(habit.getId())).thenReturn(true);

            mockMvc.perform(get("/" + habit.getId())
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(habit.getId()))
                    .andExpect(jsonPath("$.name").value("Reading"))
                    .andExpect(jsonPath("$.description").value("Read every morning"))
                    .andExpect(jsonPath("$.active").value(true));
        }

        @Test
        void getHabitById_shouldReturn403_whenUserIsNotOwner() throws Exception {
            SecurityContextHolder.clearContext();
            Person person = createSamplePerson(123L, "testuser", "ROLE_USER");
            PersonDetails personDetails = new PersonDetails(person);
            String token = jwtUtil.generateAccessToken(person.getId(), person.getUsername(), person.getRole());

            Habit habit = createSampleHabit(555L, "Reading", true, "Read every morning");
            Habit savedHabit = habitRepository.save(habit);

            Authentication auth = new UsernamePasswordAuthenticationToken(personDetails, null, personDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);

            mockMvc.perform(get("/" + habit.getId())
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403))
                    .andExpect(jsonPath("$.path").value("/" + savedHabit.getId()));
        }

        @Test
        void getHabitById_shouldThrow400Exception_whenPathIsNotCorrect() throws Exception {
            SecurityContextHolder.clearContext();
            Person person = createSamplePerson(123L, "testuser", "ROLE_USER");
            PersonDetails personDetails = new PersonDetails(person);
            String token = jwtUtil.generateAccessToken(person.getId(), person.getUsername(), person.getRole());

            Authentication auth = new UsernamePasswordAuthenticationToken(personDetails, null, personDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);

            mockMvc.perform(get("/abc")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.path").value("/abc"));
        }
    }

    @Nested
    class newHabitTests {
        @Test
        void createHabit_shouldReturn200_whenDataIsValidAndUserAuthenticated() throws Exception {
            Long personId = 123L;
            Person person = createSamplePerson(personId, "testuser", "ROLE_USER");
            PersonDetails personDetails = new PersonDetails(person);
            String token = jwtUtil.generateAccessToken(person.getId(), person.getUsername(), person.getRole());

            Authentication auth = new UsernamePasswordAuthenticationToken(personDetails, null, personDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);

            mockMvc.perform(post("/create-habit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "name": "Morning Workout",
                                        "description": "15-minute stretch",
                                        "active": true
                                    }
                                    """)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Morning Workout"))
                    .andExpect(jsonPath("$.description").value("15-minute stretch"))
                    .andExpect(jsonPath("$.active").value(true));
        }

        @Test
        void createHabit_shouldThrowValidationException_whenDataIsNotCorrect() throws Exception {
            Long personId = 123L;
            Person person = createSamplePerson(personId, "testuser", "ROLE_USER");
            PersonDetails personDetails = new PersonDetails(person);
            String token = jwtUtil.generateAccessToken(person.getId(), person.getUsername(), person.getRole());

            Authentication auth = new UsernamePasswordAuthenticationToken(personDetails, null, personDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);

            mockMvc.perform(post("/create-habit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "name": "M",
                                        "active": true
                                    }
                                    """)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error", Matchers.containsString("name - Product name must be between 2 and 255 characters;")));
        }

        @Test
        void createHabit_shouldReturn403_whenTokenIsMissing() throws Exception {
            SecurityContextHolder.clearContext();

            mockMvc.perform(get("/all-habits"))
                    .andExpect(status().isForbidden());
        }

        @Test
        void createHabit_shouldReturn400_whenBodyIsEmpty() throws Exception {
            Person person = createSamplePerson(1L, "test", "ROLE_USER");
            String token = jwtUtil.generateAccessToken(person.getId(), person.getUsername(), person.getRole());

            mockMvc.perform(post("/create-habit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("") // пустое тело
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Malformed or missing request body"));
        }

        @Test
        void createHabit_shouldReturn400_whenJsonIsMalformed() throws Exception {
            Long personId = 123L;
            Person person = createSamplePerson(personId, "user", "ROLE_USER");
            String token = jwtUtil.generateAccessToken(person.getId(), person.getUsername(), person.getRole());

            mockMvc.perform(post("/create-habit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "name": "Morning",
                                        "active": tr
                                    }
                                    """)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Malformed or missing request body"));
        }
    }

    @Nested
    class DeleteHabitTests {
        @Test
        void deleteHabit_shouldReturn200_whenUserIsOwner() throws Exception {
            // Подготовка пользователя и привычки
            Long personId = 123L;
            Person person = createSamplePerson(personId, "testuser", "ROLE_USER");
            PersonDetails personDetails = new PersonDetails(person);
            String token = jwtUtil.generateAccessToken(person.getId(), person.getUsername(), person.getRole());

            Habit habit = createSampleHabit(personId, "Test Habit", true, "desc");
            habitRepository.save(habit);

            // Мокаем isOwner
            when(habitSecurity.isOwner(habit.getId())).thenReturn(true);

            // Устанавливаем аутентификацию
            Authentication auth = new UsernamePasswordAuthenticationToken(personDetails, null, personDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);

            mockMvc.perform(delete("/delete/" + habit.getId())
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Habit with id " + habit.getId() + " successfully removed."));

            // Проверка, что событие отправлено
            verify(kafkaTemplate, times(1)).send("habit-events", "Habit deleted: " + habit.getId());
        }

        @Test
        void deleteHabit_shouldReturn403_whenUserIsNotOwner() throws Exception {
            Long personId = 123L;
            Person person = createSamplePerson(personId, "user", "ROLE_USER");
            String token = jwtUtil.generateAccessToken(person.getId(), person.getUsername(), person.getRole());

            Habit habit = createSampleHabit(999L, "Alien Habit", true, "not yours");
            habitRepository.save(habit);

            // isOwner возвращает false
            when(habitSecurity.isOwner(habit.getId())).thenReturn(false);

            Authentication auth = new UsernamePasswordAuthenticationToken(
                    new PersonDetails(person), null, List.of(() -> "ROLE_USER"));
            SecurityContextHolder.getContext().setAuthentication(auth);

            mockMvc.perform(delete("/delete/" + habit.getId())
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403))
                    .andExpect(jsonPath("$.path").value("/delete/" + habit.getId()));
        }

        @Test
        void deleteHabit_shouldReturn403_whenTokenIsMissing() throws Exception {
            SecurityContextHolder.clearContext();

            mockMvc.perform(delete("/delete/1"))
                    .andExpect(status().isForbidden()); // или .isUnauthorized(), если фильтр обрабатывает
        }

        @Test
        void deleteHabit_shouldReturn403_whenPathVariableIsInvalid() throws Exception {
            Person person = createSamplePerson(1L, "user", "ROLE_USER");
            String token = jwtUtil.generateAccessToken(person.getId(), person.getUsername(), person.getRole());

            mockMvc.perform(delete("/delete/abc")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.path").value("/delete/abc"));
        }
    }

    @Nested
    class UpdateHabitTests {
        @Test
        void updateHabit_shouldReturn200_whenUserIsOwnerAndDataIsValid() throws Exception {
            Long personId = 123L;
            Person person = createSamplePerson(personId, "testuser", "ROLE_USER");
            PersonDetails personDetails = new PersonDetails(person);
            String token = jwtUtil.generateAccessToken(person.getId(), person.getUsername(), person.getRole());

            // Сохраняем привычку
            Habit habit = createSampleHabit(personId, "Old Name", true, "Old description");
            habitRepository.save(habit);

            // Обновляем только name
            String json = """
                            {
                                "name": "Updated Name"
                            }
                            """;

            // Мокаем проверку владельца
            when(habitSecurity.isOwner(habit.getId())).thenReturn(true);

            // Аутентификация
            Authentication auth = new UsernamePasswordAuthenticationToken(personDetails, null, personDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);

            mockMvc.perform(patch("/update/" + habit.getId())
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Updated Name"))
                    .andExpect(jsonPath("$.description").value("Old description"))
                    .andExpect(jsonPath("$.active").value(true));

            // Проверка отправки события
            verify(kafkaTemplate, times(1)).send("habit-events", "Habit updated: " + habit.getId());
        }

        @Test
        void updateHabit_shouldReturn403_whenUserIsNotOwner() throws Exception {
            Long personId = 123L;
            Person person = createSamplePerson(personId, "testuser", "ROLE_USER");
            PersonDetails personDetails = new PersonDetails(person);
            String token = jwtUtil.generateAccessToken(person.getId(), person.getUsername(), person.getRole());

            Habit habit = createSampleHabit(999L, "Other habit", true, "not yours");
            habitRepository.save(habit);

            when(habitSecurity.isOwner(habit.getId())).thenReturn(false);

            Authentication auth = new UsernamePasswordAuthenticationToken(personDetails, null, personDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);

            mockMvc.perform(patch("/update/" + habit.getId())
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                            {
                                "name": "Updated Name"
                            }
                            """))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403))
                    .andExpect(jsonPath("$.path").value("/update/" + habit.getId()));
        }

        @Test
        void updateHabit_shouldReturn500_whenAllFieldsAreNull() throws Exception {
            Long personId = 123L;
            Person person = createSamplePerson(personId, "testuser", "ROLE_USER");
            PersonDetails personDetails = new PersonDetails(person);
            String token = jwtUtil.generateAccessToken(person.getId(), person.getUsername(), person.getRole());

            Habit habit = createSampleHabit(personId, "Name", true, "desc");
            habitRepository.save(habit);

            when(habitSecurity.isOwner(habit.getId())).thenReturn(true);

            Authentication auth = new UsernamePasswordAuthenticationToken(personDetails, null, personDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);

            mockMvc.perform(patch("/update/" + habit.getId())
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "name": null,
                                        "description": null,
                                        "active": null,
                                        "personId": null
                                    }
                                    """))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.error").value("Internal server error"));
        }

        @Test
        void updateHabit_shouldReturn404_whenHabitDoesNotExist() throws Exception {
            Long personId = 123L;
            Person person = createSamplePerson(personId, "testuser", "ROLE_USER");
            PersonDetails personDetails = new PersonDetails(person);
            String token = jwtUtil.generateAccessToken(person.getId(), person.getUsername(), person.getRole());

            Long nonexistentId = 999L;

            when(habitSecurity.isOwner(nonexistentId)).thenReturn(true);

            Authentication auth = new UsernamePasswordAuthenticationToken(personDetails, null, personDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);

            mockMvc.perform(patch("/update/" + nonexistentId)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "name": "Updated"
                                    }
                                    """))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("error: Habit with id 999 not found"));
        }

        @Test
        void updateHabit_shouldReturn400_whenNameIsTooShort() throws Exception {
            Long personId = 123L;
            Person person = createSamplePerson(personId, "testuser", "ROLE_USER");
            PersonDetails personDetails = new PersonDetails(person);
            String token = jwtUtil.generateAccessToken(person.getId(), person.getUsername(), person.getRole());

            Habit habit = createSampleHabit(personId, "Name", true, "desc");
            habitRepository.save(habit);

            when(habitSecurity.isOwner(habit.getId())).thenReturn(true);

            Authentication auth = new UsernamePasswordAuthenticationToken(personDetails, null, personDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);

            mockMvc.perform(patch("/update/" + habit.getId())
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "name": "A"
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error", containsString("name - Product name must be between 2 and 255 characters")));
        }
    }

    private Person createSamplePerson(Long id, String username, String role) {
        Person person = new Person();
        person.setId(id);
        person.setUsername(username);
        person.setRole(role);
        return person;
    }

    private Habit createSampleHabit(Long personId, String name, boolean active, String description) {
        Habit habit1 = new Habit();
        habit1.setPersonId(personId);
        habit1.setName(name);
        habit1.setActive(active);
        habit1.setDescription(description);
        habit1.setCreatedAt(LocalDate.now());
        return habit1;
    }
}
