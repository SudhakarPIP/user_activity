package com.useractivity.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.useractivity.dto.ActivityResponse;
import com.useractivity.dto.CreateActivityRequest;
import com.useractivity.dto.TimelineResponse;
import com.useractivity.entity.UserActivity;
import com.useractivity.exception.AlreadyDeletedException;
import com.useractivity.exception.ResourceNotFoundException;
import com.useractivity.repository.UserActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
    private final ObjectMapper objectMapper;

    @Value("${app.pagination.max-size:100}")
    private int maxPageSize;

    @Value("${app.pagination.min-size:1}")
    private int minPageSize;

    @Transactional
    public ActivityResponse createActivity(Long userId, CreateActivityRequest request) {
        // Handle null or empty metadata - MySQL JSON column requires valid JSON or null
        String metadata = request.getMetadata();
        if (metadata == null || metadata.trim().isEmpty()) {
            metadata = null;
        } else {
            // Validate JSON format
            validateJson(metadata);
        }

        UserActivity activity = UserActivity.builder()
                .userId(userId)
                .activityType(request.getActivityType().name())
                .description(request.getDescription())
                .metadata(metadata)
                .isDeleted(false)
                .build();

        UserActivity saved = repository.save(activity);
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteActivity(Long activityId) {
        UserActivity activity = repository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found with id: " + activityId));

        if (Boolean.TRUE.equals(activity.getIsDeleted())) {
            throw new AlreadyDeletedException("Activity already deleted with id: " + activityId);
        }

        activity.setIsDeleted(true);
        repository.save(activity);
    }

    public TimelineResponse getTimeline(Long userId, int page, int size) {
        log.info("\nActivityService TimeLine In........");

        validatePaginationParameters(page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<UserActivity> activityPage = repository.findByUserIdAndNotDeletedOrderByCreatedAtDesc(userId, pageable);

        List<ActivityResponse> activities = activityPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        log.info("TimeLine Results : \n{}", activities);
        return TimelineResponse.builder()
                .userId(userId)
                .page(page)
                .size(size)
                .totalElements(activityPage.getTotalElements())
                .totalPages(activityPage.getTotalPages())
                .activities(activities)
                .build();
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

    private void validateJson(String json) {
        try {
            objectMapper.readTree(json);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JSON format in metadata field");
        }
    }


    private void validatePaginationParameters(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page number must be 0 or greater. Provided: " + page);
        }

        if (size < minPageSize) {
            throw new IllegalArgumentException(
                    String.format("Page size must be at least %d. Provided: %d", minPageSize, size));
        }

        if (size > maxPageSize) {
            throw new IllegalArgumentException(
                    String.format("Page size cannot exceed %d. Provided: %d", maxPageSize, size));
        }
    }
}
