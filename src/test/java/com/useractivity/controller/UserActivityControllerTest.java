package com.useractivity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.useractivity.dto.ActivityResponse;
import com.useractivity.dto.CreateActivityRequest;
import com.useractivity.dto.TimelineResponse;
import com.useractivity.enums.ActivityType;
import com.useractivity.repository.UserActivityRepository;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
class UserActivityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserActivityRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // repository.deleteAll();
    }

    @Test
    void createActivityAPITest() throws Exception {
        log.info("\nCreateActivityAPITest Call ..........!");
        
        long initialCount = repository.count();
        log.info("Initial record count: {}", initialCount);
        
        CreateActivityRequest request = new CreateActivityRequest(
                ActivityType.LOGIN,
                "User logged in from web",
                "{\"ip\":\"192.168.1.21\"}"
        );

        String responseJson = mockMvc.perform(post("/api/v1/users/221/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.activityType").value(ActivityType.LOGIN))
                .andExpect(jsonPath("$.description").value("User logged in from web"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        ActivityResponse response = objectMapper.readValue(responseJson, ActivityResponse.class);
        assertNotNull(response);
        assertNotNull(response.getId());
//        assertEquals("LOGIN", response.getActivityType());
        assertEquals(ActivityType.LOGIN, response.getActivityType());
        
        long finalCount = repository.count();
        assertEquals(initialCount + 1, finalCount, "Activity should be saved to database");
        log.info("Final record count: " + finalCount);
        
        // Verify the saved activity
        var savedActivity = repository.findById(response.getId());
        assertTrue(savedActivity.isPresent(), "Activity should exist in database");
        assertEquals(221L, savedActivity.get().getUserId());
//        assertEquals("LOGIN", savedActivity.get().getActivityType());
        assertEquals(ActivityType.LOGIN, savedActivity.get().getActivityType());
        assertEquals("User logged in from web", savedActivity.get().getDescription());
        assertEquals(false, savedActivity.get().getIsDeleted());

        log.info("✅ Activity saved to MySQL database 'pip.user_activities' with ID: {}", response.getId());
        log.info("CreateActivityAPITest call ENDED.....!\n");
    }

    @Test
    void createActivityProfileUpdateAPITest() throws Exception {
        log.info("\nCreateActivityProfileUpdateAPITest call ..........!");

        long initialCount = repository.count();

        CreateActivityRequest request = new CreateActivityRequest(
                ActivityType.PROFILE_UPDATE,
                "User profile updated from web",
                "{\"ip\":\"192.168.1.21\"}"
        );

        String responseJson = mockMvc.perform(post("/api/v1/users/221/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.activityType").value(ActivityType.PROFILE_UPDATE))
                .andExpect(jsonPath("$.description").value("User profile updated from web"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        ActivityResponse response = objectMapper.readValue(responseJson, ActivityResponse.class);
        assertNotNull(response);
        assertNotNull(response.getId());
//        assertEquals("PROFILE_UPDATE", response.getActivityType());
        assertEquals(ActivityType.PROFILE_UPDATE, response.getActivityType());

        long finalCount = repository.count();
        assertEquals(initialCount + 1, finalCount, "Activity should be saved to database");

        var savedActivity = repository.findById(response.getId());
        assertTrue(savedActivity.isPresent(), "Activity should exist in database");
        assertEquals(221L, savedActivity.get().getUserId());
//        assertEquals("PROFILE_UPDATE", savedActivity.get().getActivityType());
        assertEquals(ActivityType.PROFILE_UPDATE, savedActivity.get().getActivityType());
        assertEquals("User profile updated from web", savedActivity.get().getDescription());
        assertEquals(false, savedActivity.get().getIsDeleted());

        log.info("✅ PROFILE_UPDATE activity saved to MySQL database with ID: {}", response.getId());
        log.info("CreateActivityProfileUpdateAPITest call ENDED.....!");
    }

    @Test
    void deleteActivityAPITest() throws Exception {
        log.info("\ndeleteActivityAPITest call ..........!");
        
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
        
        assertTrue(repository.findById(activityId).isPresent());
        assertEquals(false, repository.findById(activityId).get().getIsDeleted());

        mockMvc.perform(delete("/api/v1/activities/" + activityId))
                .andExpect(status().isNoContent());
        
        var deletedActivity = repository.findById(activityId);
        assertTrue(deletedActivity.isPresent(), "Activity should still exist after soft delete");
        assertEquals(true, deletedActivity.get().getIsDeleted(), "Activity should be marked as deleted");
        
        log.info("✅ Activity soft deleted successfully in MySQL database");
        log.info("deleteActivityAPITest test ENDED.....!");
    }

    @Test
    void timelineAPITest() throws Exception {
        log.info("\nTimelineAPITest call ..........!");
        // Arrange: create two activities for user 135
        CreateActivityRequest req1 = new CreateActivityRequest(
                ActivityType.LOGIN,
                "First login",
                "{\"ip\":\"192.168.1.10\"}"
        );
        CreateActivityRequest req2 = new CreateActivityRequest(
                ActivityType.PROFILE_UPDATE,
                "User profile update",
                "{\"ip\":\"192.168.1.10\"}"
        );

        log.info("\n ACTIVITY 1 creating ........!!!!!");
        mockMvc.perform(post("/api/v1/users/135/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isCreated());

        log.info("\n ACTIVITY 2 creating ........!!!!!");
        mockMvc.perform(post("/api/v1/users/135/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isCreated());

        // Act: fetch timeline
        log.info("\n TIMELINE call  ........!!!!!");
        String timelineResponse = mockMvc.perform(get("/api/v1/users/135/activities/timeline")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(135))
                .andExpect(jsonPath("$.activities").isArray())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Assert: verify payload
        TimelineResponse timeline = objectMapper.readValue(timelineResponse, TimelineResponse.class);
        assertNotNull(timeline);
        assertEquals(135L, timeline.getUserId());
        assertTrue(timeline.getTotalElements() >= 2);
        assertTrue(timeline.getActivities().size() >= 2);

        log.info("✅ Activity Timelines fetched successfully from DB");
        log.info("TimelineAPITest call ENDED.....!");
    }
}