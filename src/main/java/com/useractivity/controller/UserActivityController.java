package com.useractivity.controller;

import com.useractivity.dto.ActivityResponse;
import com.useractivity.dto.CreateActivityRequest;
import com.useractivity.dto.TimelineResponse;
import com.useractivity.service.UserActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Activity", description = "APIs for managing user activities")
public class UserActivityController {
    
    private final UserActivityService userActivityService;
    
    @PostMapping("/users/{userId}/activities")
    @Operation(summary = "Create a new activity", description = "Records a new user activity")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Activity created successfully",
                content = @Content(schema = @Schema(implementation = ActivityResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ActivityResponse> createActivity(
            @Parameter(description = "User ID", required = true) @PathVariable Long userId,
            @Valid @RequestBody CreateActivityRequest request) {

        log.info("\nUserActivityController->CreateActivity In ... for userId: {}", userId);
        ActivityResponse response = userActivityService.createActivity(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @DeleteMapping("/activities/{activityId}")
    @Operation(summary = "Delete an activity", description = "Soft deletes an existing activity by setting is_deleted = true")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Activity deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Activity not found"),
            @ApiResponse(responseCode = "400", description = "Activity already deleted"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteActivity(
            @Parameter(description = "Activity ID", required = true) @PathVariable Long activityId) {

        log.info("Deleting activity with ID: {}", activityId);
        userActivityService.deleteActivity(activityId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/{userId}/activities/timeline")
    @Operation(summary = "Get user timeline", description = "Retrieves paginated and sorted timeline of user activities")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Timeline retrieved successfully",
                    content = @Content(schema = @Schema(implementation = TimelineResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<TimelineResponse> getTimeline(
            @Parameter(description = "User ID", required = true) @PathVariable Long userId,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {

        log.info("\nTimeLine params : UsersID :{} & page: {} && Size: {}", userId, page, size);
        TimelineResponse response = userActivityService.getTimeline(userId, page, size);
        return ResponseEntity.ok(response);
    }
}


