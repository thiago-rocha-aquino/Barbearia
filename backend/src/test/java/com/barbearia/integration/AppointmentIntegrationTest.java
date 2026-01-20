package com.barbearia.integration;

import com.barbearia.application.dto.AppointmentDTO;
import com.barbearia.application.dto.AuthDTO;
import com.barbearia.domain.entity.Service;
import com.barbearia.domain.entity.User;
import com.barbearia.domain.entity.WorkingHours;
import com.barbearia.domain.enums.DayOfWeekEnum;
import com.barbearia.domain.enums.UserRole;
import com.barbearia.domain.repository.ServiceRepository;
import com.barbearia.domain.repository.UserRepository;
import com.barbearia.domain.repository.WorkingHoursRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional
class AppointmentIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("barbearia_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("app.notifications.enabled", () -> "false");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private WorkingHoursRepository workingHoursRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User barber;
    private Service service;
    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        barber = userRepository.save(User.builder()
                .name("Barbeiro Test")
                .email("barber@integration.test")
                .password(passwordEncoder.encode("password123"))
                .role(UserRole.ADMIN)
                .active(true)
                .build());

        for (DayOfWeekEnum day : DayOfWeekEnum.values()) {
            workingHoursRepository.save(WorkingHours.builder()
                    .barber(barber)
                    .dayOfWeek(day)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(18, 0))
                    .isWorking(day != DayOfWeekEnum.SUNDAY)
                    .build());
        }

        service = serviceRepository.save(Service.builder()
                .name("Corte Test")
                .description("Corte para teste")
                .durationMinutes(30)
                .bufferMinutes(0)
                .price(BigDecimal.valueOf(35))
                .active(true)
                .build());

        AuthDTO.LoginRequest loginRequest = new AuthDTO.LoginRequest();
        loginRequest.setEmail("barber@integration.test");
        loginRequest.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        AuthDTO.LoginResponse loginResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                AuthDTO.LoginResponse.class
        );
        authToken = loginResponse.getAccessToken();
    }

    @Test
    @DisplayName("Deve criar agendamento público com sucesso")
    void shouldCreatePublicAppointmentSuccessfully() throws Exception {
        LocalDate futureDate = LocalDate.now().plusDays(2);
        if (futureDate.getDayOfWeek().getValue() == 7) {
            futureDate = futureDate.plusDays(1);
        }

        LocalDateTime startTime = futureDate.atTime(10, 0);

        AppointmentDTO.PublicCreateRequest request = AppointmentDTO.PublicCreateRequest.builder()
                .serviceId(service.getId())
                .barberId(barber.getId())
                .startTime(startTime)
                .clientName("Cliente Integration Test")
                .clientPhone("11999998888")
                .clientEmail("cliente@test.com")
                .build();

        mockMvc.perform(post("/api/booking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.clientName").value("Cliente Integration Test"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.cancellationToken").exists());
    }

    @Test
    @DisplayName("Deve rejeitar agendamento com conflito")
    void shouldRejectAppointmentWithConflict() throws Exception {
        LocalDate futureDate = LocalDate.now().plusDays(2);
        if (futureDate.getDayOfWeek().getValue() == 7) {
            futureDate = futureDate.plusDays(1);
        }

        LocalDateTime startTime = futureDate.atTime(11, 0);

        AppointmentDTO.PublicCreateRequest request1 = AppointmentDTO.PublicCreateRequest.builder()
                .serviceId(service.getId())
                .barberId(barber.getId())
                .startTime(startTime)
                .clientName("Cliente 1")
                .clientPhone("11999997777")
                .build();

        mockMvc.perform(post("/api/booking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        AppointmentDTO.PublicCreateRequest request2 = AppointmentDTO.PublicCreateRequest.builder()
                .serviceId(service.getId())
                .barberId(barber.getId())
                .startTime(startTime)
                .clientName("Cliente 2")
                .clientPhone("11999996666")
                .build();

        mockMvc.perform(post("/api/booking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Deve buscar horários disponíveis")
    void shouldGetAvailableSlots() throws Exception {
        LocalDate futureDate = LocalDate.now().plusDays(3);
        if (futureDate.getDayOfWeek().getValue() == 7) {
            futureDate = futureDate.plusDays(1);
        }

        mockMvc.perform(get("/api/public/availability/slots")
                        .param("serviceId", service.getId().toString())
                        .param("barberId", barber.getId().toString())
                        .param("date", futureDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].available").exists());
    }

    @Test
    @DisplayName("Admin deve cancelar agendamento")
    void adminShouldCancelAppointment() throws Exception {
        LocalDate futureDate = LocalDate.now().plusDays(2);
        if (futureDate.getDayOfWeek().getValue() == 7) {
            futureDate = futureDate.plusDays(1);
        }

        LocalDateTime startTime = futureDate.atTime(14, 0);

        AppointmentDTO.PublicCreateRequest createRequest = AppointmentDTO.PublicCreateRequest.builder()
                .serviceId(service.getId())
                .barberId(barber.getId())
                .startTime(startTime)
                .clientName("Cliente Cancel Test")
                .clientPhone("11999995555")
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/booking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        AppointmentDTO.Response createdAppointment = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                AppointmentDTO.Response.class
        );

        mockMvc.perform(post("/api/admin/appointments/" + createdAppointment.getId() + "/cancel")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED_BY_ADMIN"));
    }

    @Test
    @DisplayName("Deve listar serviços públicos")
    void shouldListPublicServices() throws Exception {
        mockMvc.perform(get("/api/public/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
