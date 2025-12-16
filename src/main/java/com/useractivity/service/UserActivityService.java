package com.useractivity.service;

import com.useractivity.dto.ActivityResponse;
import com.useractivity.dto.CreateActivityRequest;
import com.useractivity.dto.TimelineResponse;
import com.useractivity.entity.UserActivity;
import com.useractivity.exception.ResourceNotFoundException;
import com.useractivity.repository.UserActivityRepository;
import jakarta.xml.bind.SchemaOutputResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserActivityService {

    private final UserActivityRepository repository;

    @Transactional
    public ActivityResponse createActivity(Long userId, CreateActivityRequest request) {
        log.info("Creating activity for user: {}", userId);
        System.out.println("createActivityService.. In with user_id :"+userId);
        UserActivity activity = UserActivity.builder()
                .userId(userId)
                .activityType(request.getActivityType().name())
                .description(request.getDescription())
                .metadata(request.getMetadata())
                .isDeleted(false)
                .build();

        UserActivity saved = repository.save(activity);
        return mapToResponse(saved);
    }

    // add inside UserActivityService
    @Transactional
    public Long createAndDeleteActivity(Long activityId) {
        // Try to reuse context from the target activity if it exists
        UserActivity existing = repository.findById(activityId).orElse(null);

        UserActivity marker = UserActivity.builder()
                .userId(existing != null ? existing.getUserId() : 0L)
                .activityType(existing != null ? existing.getActivityType() : "DELETE")
                .description(existing != null ? existing.getDescription() : "Deletion placeholder for activity " + activityId)
                .metadata(existing != null ? existing.getMetadata() : null)
                .isDeleted(false)
                .build();

        // Create the record
        UserActivity saved = repository.save(marker);

        // Immediately mark it deleted
        saved.setIsDeleted(true);
        repository.save(saved);

        return saved.getId();
    }

    @Transactional
    public void deleteActivityOld(Long activityId) {
//        log.info("Service -> Soft deleting activity: {}", activityId);
        System.out.println("\nService -> deleteActivity for activityId: "+activityId);
        UserActivity activity = repository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found with id: " + activityId));

        if (Boolean.TRUE.equals(activity.getIsDeleted())) {
            throw new RuntimeException("Activity already deleted");
        }

        activity.setIsDeleted(true);

        repository.save(activity);
    }

    private ActivityResponse mapToResponse(UserActivity activity) {
        return ActivityResponse.builder()
                .id(activity.getId())
                .activityType(activity.getActivityType())
                .description(activity.getDescription())
                .metadata(activity.getMetadata())
                .createdAt(activity.getCreatedAt())
                .build();
    }
}