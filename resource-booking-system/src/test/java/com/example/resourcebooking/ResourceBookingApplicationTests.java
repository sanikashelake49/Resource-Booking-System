package com.example.resourcebooking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end tests using MockMvc against the real Spring context
 * (with an in-memory H2 database). Covers authentication, RBAC,
 * ownership enforcement, validation, filtering, pagination,
 * sorting and booking-conflict rules described in the assignment.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ResourceBookingApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() throws Exception {
        adminToken = login("admin", "admin123");
        userToken = login("user", "user123");
    }

    private String login(String username, String password) throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("username", username);
        body.put("password", password);

        String response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("token").asText();
    }

    // 1. Login success
    @Test
    void loginSuccess() throws Exception {
        Map<String, String> body = Map.of("username", "admin", "password", "admin123");
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    // 2. Login invalid password
    @Test
    void loginInvalidPassword() throws Exception {
        Map<String, String> body = Map.of("username", "admin", "password", "wrong");
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }

    // 3. JWT protected endpoint - no token
    @Test
    void protectedEndpointWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/resources"))
                .andExpect(status().isUnauthorized());
    }

    // 4. USER can read resources
    @Test
    void userCanReadResources() throws Exception {
        mockMvc.perform(get("/resources").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());
    }

    // 5. USER cannot create resource
    @Test
    void userCannotCreateResource() throws Exception {
        Map<String, Object> body = Map.of(
                "name", "New Room", "price", 100.0, "available", true);
        mockMvc.perform(post("/resources")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }

    // 6. ADMIN can create resource
    @Test
    void adminCanCreateResource() throws Exception {
        Map<String, Object> body = Map.of(
                "name", "New Room", "price", 100.0, "available", true);
        mockMvc.perform(post("/resources")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

    // 7. ADMIN can update resource
    @Test
    void adminCanUpdateResource() throws Exception {
        Long id = createResourceAsAdmin("Room to update", 200.0);
        Map<String, Object> update = Map.of(
                "name", "Updated Room", "price", 250.0, "available", true);
        mockMvc.perform(put("/resources/" + id)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Room"));
    }

    // 8. ADMIN can delete resource
    @Test
    void adminCanDeleteResource() throws Exception {
        Long id = createResourceAsAdmin("Room to delete", 50.0);
        mockMvc.perform(delete("/resources/" + id)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    // 9. USER can create reservation
    @Test
    void userCanCreateReservation() throws Exception {
        Long resourceId = createResourceAsAdmin("Bookable Room", 500.0);
        Map<String, Object> body = reservationBody(resourceId, 10, 12, 1000.0);

        mockMvc.perform(post("/reservations")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.username").value("user"));
    }

    // 10. USER can view own reservations
    @Test
    void userCanViewOwnReservations() throws Exception {
        Long resourceId = createResourceAsAdmin("Own Res Room", 500.0);
        createReservationAsUser(resourceId, 8, 9);

        mockMvc.perform(get("/reservations").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    // 11. USER cannot view another user's reservation
    @Test
    void userCannotViewAnotherUsersReservation() throws Exception {
        Long resourceId = createResourceAsAdmin("Admin Only Room", 700.0);
        Long reservationId = createReservationAsAdmin(resourceId, 14, 15);

        mockMvc.perform(get("/reservations/" + reservationId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    // 12. ADMIN can view all reservations
    @Test
    void adminCanViewAllReservations() throws Exception {
        mockMvc.perform(get("/reservations").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    // 13. Invalid price rejected
    @Test
    void invalidPriceRejected() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("name", "Bad Price Room");
        body.put("price", -50.0);
        body.put("available", true);

        mockMvc.perform(post("/resources")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    // 14. Invalid start/end time rejected
    @Test
    void invalidStartEndTimeRejected() throws Exception {
        Long resourceId = createResourceAsAdmin("Time Check Room", 300.0);
        Map<String, Object> body = reservationBody(resourceId, 12, 10, 500.0); // end before start

        mockMvc.perform(post("/reservations")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    // 15. Reservation overlap rejected
    @Test
    void overlappingReservationRejected() throws Exception {
        Long resourceId = createResourceAsAdmin("Overlap Room", 400.0);
        createReservationAsUser(resourceId, 10, 12);

        Map<String, Object> overlapping = reservationBody(resourceId, 11, 13, 400.0);
        mockMvc.perform(post("/reservations")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(overlapping)))
                .andExpect(status().isConflict());
    }

    // 16. Reservation filtering
    @Test
    void reservationFilteringByStatus() throws Exception {
        mockMvc.perform(get("/reservations?status=PENDING")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    // 17. Pagination
    @Test
    void pagination() throws Exception {
        mockMvc.perform(get("/reservations?page=0&size=5")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(5));
    }

    // 18. Sorting
    @Test
    void sorting() throws Exception {
        mockMvc.perform(get("/reservations?sort=price,desc")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    // 19. Unauthorized request returns 401
    @Test
    void unauthorizedRequestReturns401() throws Exception {
        mockMvc.perform(get("/reservations"))
                .andExpect(status().isUnauthorized());
    }

    // 20. Forbidden request returns 403
    @Test
    void forbiddenRequestReturns403() throws Exception {
        mockMvc.perform(delete("/resources/1")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    // ---------- helpers ----------

    private Long createResourceAsAdmin(String name, double price) throws Exception {
        Map<String, Object> body = Map.of("name", name, "price", price, "available", true);
        String response = mockMvc.perform(post("/resources")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private Map<String, Object> reservationBody(Long resourceId, int startHour, int endHour, double price) {
        LocalDateTime base = LocalDateTime.now().plusDays(5).withMinute(0).withSecond(0).withNano(0);
        Map<String, Object> body = new HashMap<>();
        body.put("resourceId", resourceId);
        body.put("startTime", base.withHour(startHour).toString());
        body.put("endTime", base.withHour(endHour).toString());
        body.put("price", price);
        return body;
    }

    private Long createReservationAsUser(Long resourceId, int startHour, int endHour) throws Exception {
        Map<String, Object> body = reservationBody(resourceId, startHour, endHour, 500.0);
        String response = mockMvc.perform(post("/reservations")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private Long createReservationAsAdmin(Long resourceId, int startHour, int endHour) throws Exception {
        Map<String, Object> body = reservationBody(resourceId, startHour, endHour, 500.0);
        String response = mockMvc.perform(post("/reservations")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }
}
