package com.example.habit_service.controller;

import com.example.habit_service.HabitServiceApplication;
import com.example.habit_service.dto.UserDeletedEvent;
import com.example.habit_service.entity.Habit;
import com.example.habit_service.repository.HabitRepository;
import com.example.habit_service.security.JWTUtil;
import com.example.habit_service.security.Person;
import com.example.habit_service.security.PersonDetails;
import com.example.habit_service.service.HabitEventPublisher;
import com.example.habit_service.service.HabitService;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
/*@EnableAutoConfiguration(exclude = {
        KafkaAutoConfiguration.class
})*/
@AutoConfigureMockMvc
@SpringBootTest(classes = {HabitServiceApplication.class, TestKafkaConfig.class})
public class HabitControllerTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private HabitRepository habitRepository;
    @Autowired private HabitService habitService;
    @Autowired private JWTUtil jwtUtil;

    @MockBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @MockBean
    private HabitEventPublisher habitEventPublisher;

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
    }

    /*@TestConfiguration
    public class TestKafkaConfig {

        @Bean
        public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
            ConcurrentKafkaListenerContainerFactory<String, String> factory =
                    new ConcurrentKafkaListenerContainerFactory<>();

            factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(testConsumerConfigs()));
            factory.setAutoStartup(false); // 💡 Отключаем автоматический запуск

            return factory;
        }

        private Map<String, Object> testConsumerConfigs() {
            Map<String, Object> props = new HashMap<>();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"); // dummy
            props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            return props;
        }

        @Bean
        public KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry() {
            KafkaListenerEndpointRegistry registry = new KafkaListenerEndpointRegistry();
            return registry;
        }
    }*/


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
