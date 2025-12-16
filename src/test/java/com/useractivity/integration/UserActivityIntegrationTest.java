package com.useractivity.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Slf4j
class UserActivityIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private UserActivityRepository repository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }
    
    @Test
    void testCreateUserActivity() throws Exception {
        // Create activity
        log.info("\nCREATE USER ACTIVITY.................!!!!!");
        CreateActivityRequest request = new CreateActivityRequest(
                ActivityType.LOGIN,
                "User logged in from web",
                "{\"ip\":\"192.168.1.10\"}"
        );
        
        String createResponse = mockMvc.perform(post("/api/v1/users/123/activities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        assertNotNull(createResponse);
        
        // Get timeline
        String timelineResponse = mockMvc.perform(get("/api/v1/users/123/activities/timeline")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        TimelineResponse timeline = objectMapper.readValue(timelineResponse, TimelineResponse.class);
        assertNotNull(timeline);
        assertEquals(123L, timeline.getUserId());
        assertTrue(timeline.getTotalElements() > 0);

        log.info("CREATE USER ACTIVITY.................ENDED.!\n");
    }
    
    @Test
    void testDeleteActivity() throws Exception {
        // Create activity first
        CreateActivityRequest request = new CreateActivityRequest(
                ActivityType.LOGIN,
                "User logged in",
                null
        );
        
        mockMvc.perform(post("/api/v1/users/101/activities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
        
        // Get the created activity ID (simplified - in real scenario, parse response)
        Long activityId = repository.findAll().get(0).getId();
        System.out.println("\n$$$$$$ activityId >> "+activityId);
        
        // Delete activity
        mockMvc.perform(delete("/api/v1/activities/" + activityId))
                .andExpect(status().isNoContent());
        
        // Verify soft delete
        assertTrue(repository.findById(activityId).get().getIsDeleted());
    }
}


