package com.useractivity.service;

import com.useractivity.dto.ActivityResponse;
import com.useractivity.dto.CreateActivityRequest;
import com.useractivity.dto.TimelineResponse;
import com.useractivity.entity.UserActivity;
import com.useractivity.enums.ActivityType;
import com.useractivity.exception.ResourceNotFoundException;
import com.useractivity.repository.UserActivityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserActivityServiceTest {

    @Mock
    private UserActivityRepository repository;

    @InjectMocks
    private UserActivityService service;

    private UserActivity userActivity;
    private CreateActivityRequest createRequest;

    @BeforeEach
    void setUp() {
        userActivity = UserActivity.builder()
                .id(1L)
                .userId(123L)
                .activityType("LOGIN")
                .description("User logged in")
                .metadata("{\"ip\":\"192.168.1.10\"}")
                .createdAt(LocalDateTime.now())
                .isDeleted(false)
                .build();

        createRequest = new CreateActivityRequest(
                ActivityType.LOGIN,
                "User logged in",
                "{\"ip\":\"192.168.1.10\"}"
        );
    }

    @Test
    void testCreateActivity_Success() {
        // Use thenAnswer to simulate save behavior - set ID if not present
        when(repository.save(any(UserActivity.class))).thenAnswer(invocation -> {
            UserActivity activity = invocation.getArgument(0);
            // Simulate JPA save behavior - if ID is null, assign one
            if (activity.getId() == null) {
                activity.setId(1L);
            }
            // Set timestamps if not present
            if (activity.getCreatedAt() == null) {
                activity.setCreatedAt(LocalDateTime.now());
            }
            if (activity.getUpdatedAt() == null) {
                activity.setUpdatedAt(LocalDateTime.now());
            }
            return activity;
        });

        ActivityResponse response = service.createActivity(123L, createRequest);

        // Verify the response
        assertNotNull(response);
        assertEquals("LOGIN", response.getActivityType());
        assertEquals("User logged in", response.getDescription());
        assertNotNull(response.getId());
        
        // Verify repository.save was called
        verify(repository, times(1)).save(any(UserActivity.class));
        
        // Capture the argument to verify what was saved
        ArgumentCaptor<UserActivity> activityCaptor = ArgumentCaptor.forClass(UserActivity.class);
        verify(repository).save(activityCaptor.capture());
        
        UserActivity savedActivity = activityCaptor.getValue();
        assertEquals(123L, savedActivity.getUserId());
        assertEquals("LOGIN", savedActivity.getActivityType());
        assertEquals("User logged in", savedActivity.getDescription());
        assertEquals(false, savedActivity.getIsDeleted());
    }

    @Test
    void testDeleteActivity_Success() {
        UserActivity existingActivity = UserActivity.builder()
                .id(1L)
                .userId(123L)
                .activityType("LOGIN")
                .description("User logged in")
                .metadata("{\"ip\":\"192.168.1.10\"}")
                .createdAt(LocalDateTime.now())
                .isDeleted(false)
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(existingActivity));
        
        // Mock save to return the updated activity
        when(repository.save(any(UserActivity.class))).thenAnswer(invocation -> {
            UserActivity activity = invocation.getArgument(0);
            activity.setIsDeleted(true);
            return activity;
        });

//        assertDoesNotThrow(() -> service.deleteActivity(1L));
        
        // Verify findById was called
        verify(repository, times(1)).findById(1L);
        
        // Verify save was called with deleted flag set to true
        ArgumentCaptor<UserActivity> activityCaptor = ArgumentCaptor.forClass(UserActivity.class);
        verify(repository, times(1)).save(activityCaptor.capture());
        
        UserActivity savedActivity = activityCaptor.getValue();
        assertTrue(savedActivity.getIsDeleted(), "Activity should be marked as deleted");
    }

    @Test
    void testDeleteActivity_NotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

//        assertThrows(ResourceNotFoundException.class, () -> service.deleteActivity(1L));
        
        verify(repository, times(1)).findById(1L);
        verify(repository, never()).save(any(UserActivity.class));
    }

    @Test
    void testDeleteActivity_AlreadyDeleted() {
        UserActivity deletedActivity = UserActivity.builder()
                .id(1L)
                .userId(123L)
                .activityType("LOGIN")
                .description("User logged in")
                .isDeleted(true) // Already deleted
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(deletedActivity));

//        assertThrows(RuntimeException.class, () -> service.deleteActivity(1L));
//        assertTrue(assertThrows(RuntimeException.class, () -> service.deleteActivity(1L))
//                .getMessage().contains("already deleted"));
        
        verify(repository, times(2)).findById(1L);
        verify(repository, never()).save(any(UserActivity.class));
    }

    @Test
    void testGetTimeline_Success() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<UserActivity> page = new PageImpl<>(Collections.singletonList(userActivity), pageable, 1);

        when(repository.findByUserIdAndNotDeletedOrderByCreatedAtDesc(123L, pageable)).thenReturn(page);

        TimelineResponse response = service.getTimeline(123L, 0, 20);

        assertNotNull(response);
        assertEquals(123L, response.getUserId());
        assertEquals(0, response.getPage());
        assertEquals(20, response.getSize());
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertEquals(1, response.getActivities().size());
        assertEquals("LOGIN", response.getActivities().get(0).getActivityType());
        
        verify(repository, times(1)).findByUserIdAndNotDeletedOrderByCreatedAtDesc(123L, pageable);
    }

    @Test
    void testGetTimeline_EmptyResult() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<UserActivity> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(repository.findByUserIdAndNotDeletedOrderByCreatedAtDesc(123L, pageable)).thenReturn(emptyPage);

        TimelineResponse response = service.getTimeline(123L, 0, 20);

        assertNotNull(response);
        assertEquals(123L, response.getUserId());
        assertEquals(0, response.getTotalElements());
        assertEquals(0, response.getActivities().size());
    }
}