package com.useractivity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.useractivity.dto.ActivityResponse;
import com.useractivity.dto.CreateActivityRequest;
import com.useractivity.dto.TimelineResponse;
import com.useractivity.enums.ActivityType;
import com.useractivity.repository.UserActivityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserActivityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserActivityRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Optional: Clean up before each test (uncomment if needed)
        // repository.deleteAll();
    }

    @Test
    void testCreateActivity_Success() throws Exception {
        System.out.println("\ntestCreateActivity_Success test ..........!");
        
        // Get initial count
        long initialCount = repository.count();
        System.out.println("Initial record count: " + initialCount);
        
        CreateActivityRequest request = new CreateActivityRequest(
                ActivityType.LOGIN,
                "User logged in from web",
                "{\"ip\":\"192.168.1.21\"}"
        );

        String responseJson = mockMvc.perform(post("/api/v1/users/121/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.activityType").value("LOGIN"))
                .andExpect(jsonPath("$.description").value("User logged in from web"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Verify response
        ActivityResponse response = objectMapper.readValue(responseJson, ActivityResponse.class);
        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("LOGIN", response.getActivityType());
        
        // Wait a moment for transaction to commit
        Thread.sleep(100);
        
        // Verify data was actually saved to MySQL database
        long finalCount = repository.count();
        assertEquals(initialCount + 1, finalCount, "Activity should be saved to database");
        System.out.println("Final record count: " + finalCount);
        
        // Verify the saved activity
        var savedActivity = repository.findById(response.getId());
        assertTrue(savedActivity.isPresent(), "Activity should exist in database");
        assertEquals(121L, savedActivity.get().getUserId());
        assertEquals("LOGIN", savedActivity.get().getActivityType());
        assertEquals("User logged in from web", savedActivity.get().getDescription());
        assertEquals(false, savedActivity.get().getIsDeleted());
        
        System.out.println("✅ Activity saved to MySQL database 'pip.user_activities' with ID: " + response.getId());
        System.out.println("testCreateActivity_Success test ENDED.....!");
    }

    @Test
    void testCreateActivity_ValidationFailure() throws Exception {
        System.out.println("\ntestCreateActivity_ValidationFailure test ..........!");
        
        long initialCount = repository.count();
        
        CreateActivityRequest request = new CreateActivityRequest(
                null, // Invalid: activityType is null
                "",   // Invalid: description is blank
                null
        );

        mockMvc.perform(post("/api/v1/users/101/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").exists());

        // Verify nothing was saved to database
        Thread.sleep(100);
        long finalCount = repository.count();
        assertEquals(initialCount, finalCount, "No activity should be saved on validation failure");
        
        System.out.println("testCreateActivity_ValidationFailure test ENDED.....!");
    }

    @Test
    void testDeleteActivity_Success() throws Exception {
        System.out.println("\nDeleteActivity_Success test ..........!");
        
        // First create an activity
        CreateActivityRequest createRequest = new CreateActivityRequest(
                ActivityType.LOGIN,
                "User logged in from web",
                "{\"ip\":\"192.168.1.21\"}"
        );

        String createResponse = mockMvc.perform(post("/api/v1/users/101/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        ActivityResponse created = objectMapper.readValue(createResponse, ActivityResponse.class);
        Long activityId = created.getId();
        
        Thread.sleep(50);
        
        // Verify it exists and is not deleted
        assertTrue(repository.findById(activityId).isPresent());
        assertEquals(false, repository.findById(activityId).get().getIsDeleted());
        System.out.println("\nTEst> activityID > "+ activityId);

        // Now delete it
        mockMvc.perform(delete("/api/v1/activities/" + activityId))
                .andExpect(status().isNoContent());
        
        Thread.sleep(100);


        // Verify soft delete - record still exists but is_deleted = true
        var deletedActivity = repository.findById(activityId);
        assertTrue(deletedActivity.isPresent(), "Activity should still exist after soft delete");
        assertEquals(true, deletedActivity.get().getIsDeleted(), "Activity should be marked as deleted");
        
        System.out.println("✅ Activity soft deleted successfully in MySQL database");
        System.out.println("testDeleteActivity_Success test ENDED.....!");
    }

}