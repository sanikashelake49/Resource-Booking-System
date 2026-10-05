package com.example.resourcebooking.config;

import com.example.resourcebooking.entity.Reservation;
import com.example.resourcebooking.entity.Resource;
import com.example.resourcebooking.entity.User;
import com.example.resourcebooking.enums.ReservationStatus;
import com.example.resourcebooking.enums.Role;
import com.example.resourcebooking.repository.ReservationRepository;
import com.example.resourcebooking.repository.ResourceRepository;
import com.example.resourcebooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Seeds an ADMIN user, a regular USER, and a handful of sample
 * resources/reservations on application startup, so the API can be
 * exercised immediately (Swagger, Postman, tests) without manual setup.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;
    private final ReservationRepository reservationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUsers();
        seedResourcesAndReservations();
    }

    private void seedUsers() {
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .email("admin@resourcebooking.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(admin);
            log.info("Seeded ADMIN user (username=admin)");
        }

        if (!userRepository.existsByUsername("user")) {
            User user = User.builder()
                    .username("user")
                    .email("user@resourcebooking.com")
                    .password(passwordEncoder.encode("user123"))
                    .role(Role.USER)
                    .build();
            userRepository.save(user);
            log.info("Seeded USER user (username=user)");
        }
    }

    private void seedResourcesAndReservations() {
        if (resourceRepository.count() > 0) {
            return;
        }

        Resource roomA = resourceRepository.save(Resource.builder()
                .name("Conference Room A")
                .description("Large meeting room with projector, seats 12")
                .type("ROOM")
                .location("Pune HQ - 2nd Floor")
                .price(new BigDecimal("1500.00"))
                .available(true)
                .build());

        Resource roomB = resourceRepository.save(Resource.builder()
                .name("Conference Room B")
                .description("Small meeting room, seats 4")
                .type("ROOM")
                .location("Pune HQ - 3rd Floor")
                .price(new BigDecimal("800.00"))
                .available(true)
                .build());

        Resource car = resourceRepository.save(Resource.builder()
                .name("Company Car")
                .description("Sedan, available for official trips")
                .type("VEHICLE")
                .location("Pune HQ - Parking")
                .price(new BigDecimal("2500.00"))
                .available(true)
                .build());

        Resource projector = resourceRepository.save(Resource.builder()
                .name("Projector")
                .description("Portable HD projector")
                .type("EQUIPMENT")
                .location("Pune HQ - Store Room")
                .price(new BigDecimal("300.00"))
                .available(true)
                .build());

        log.info("Seeded {} sample resources", 4);

        User user = userRepository.findByUsername("user").orElse(null);
        User admin = userRepository.findByUsername("admin").orElse(null);

        if (user != null && admin != null) {
            // Non-overlapping sample reservations to avoid booking conflicts.
            reservationRepository.save(Reservation.builder()
                    .resource(roomA)
                    .user(user)
                    .startTime(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0))
                    .endTime(LocalDateTime.now().plusDays(1).withHour(12).withMinute(0))
                    .price(new BigDecimal("3000.00"))
                    .status(ReservationStatus.PENDING)
                    .build());

            reservationRepository.save(Reservation.builder()
                    .resource(car)
                    .user(admin)
                    .startTime(LocalDateTime.now().plusDays(2).withHour(9).withMinute(0))
                    .endTime(LocalDateTime.now().plusDays(2).withHour(17).withMinute(0))
                    .price(new BigDecimal("2500.00"))
                    .status(ReservationStatus.CONFIRMED)
                    .build());

            log.info("Seeded sample reservations");
        }
    }
}
