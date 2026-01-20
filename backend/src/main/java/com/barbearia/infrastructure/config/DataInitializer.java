package com.barbearia.infrastructure.config;

import com.barbearia.application.service.ServiceService;
import com.barbearia.application.service.UserService;
import com.barbearia.application.service.WorkingHoursService;
import com.barbearia.domain.entity.User;
import com.barbearia.domain.repository.ServiceRepository;
import com.barbearia.domain.repository.WorkingHoursRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UserService userService;
    private final WorkingHoursService workingHoursService;
    private final WorkingHoursRepository workingHoursRepository;
    private final ServiceRepository serviceRepository;

    @Value("${app.admin.default-email:admin@barbearia.com}")
    private String adminEmail;

    @Value("${app.admin.default-password:admin123}")
    private String adminPassword;

    @Value("${app.admin.default-name:Administrador}")
    private String adminName;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            log.info("Initializing default data...");

            User admin = userService.createDefaultAdmin(adminEmail, adminPassword, adminName);
            log.info("Admin user ready: {}", adminEmail);

            if (workingHoursRepository.findByBarberId(admin.getId()).isEmpty()) {
                workingHoursService.createDefaultWorkingHours(admin);
                log.info("Default working hours created for admin");
            }

            if (serviceRepository.count() == 0) {
                createDefaultServices();
                log.info("Default services created");
            }

            log.info("Data initialization completed");
        };
    }

    private void createDefaultServices() {
        var services = new Object[][] {
                {"Corte de Cabelo", "Corte masculino tradicional", 30, 0, 35.00, 1},
                {"Barba", "Modelagem e aparação de barba", 20, 0, 25.00, 2},
                {"Corte + Barba", "Combo corte de cabelo e barba", 45, 5, 55.00, 3},
                {"Pigmentação", "Pigmentação de cabelo ou barba", 60, 10, 80.00, 4},
                {"Sobrancelha", "Design de sobrancelha masculina", 15, 0, 15.00, 5}
        };

        for (Object[] s : services) {
            com.barbearia.domain.entity.Service service = com.barbearia.domain.entity.Service.builder()
                    .name((String) s[0])
                    .description((String) s[1])
                    .durationMinutes((Integer) s[2])
                    .bufferMinutes((Integer) s[3])
                    .price(java.math.BigDecimal.valueOf((Double) s[4]))
                    .displayOrder((Integer) s[5])
                    .active(true)
                    .build();
            serviceRepository.save(service);
        }
    }
}
