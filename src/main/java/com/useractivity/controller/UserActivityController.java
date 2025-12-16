package com.useractivity.controller;

import com.useractivity.dto.ActivityResponse;
import com.useractivity.dto.CreateActivityRequest;
import com.useractivity.dto.TimelineResponse;
import com.useractivity.service.UserActivityService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "User Activity", description = "APIs for managing user activities")
public class UserActivityController {

    private final UserActivityService userActivityService;

    @Value("${app.pagination.max-size:100}")
    private int maxPageSize;

    @Value("${app.pagination.min-size:1}")
    private int minPageSize;

    @PostMapping("/users/{userId}/activities")
    @Operation(
            summary = "Create a new user activity",
            description = "Records a new activity for a specific user. The activity type must be one of: LOGIN, LOGOUT, PASSWORD_CHANGE, or PROFILE_UPDATE. " +
                    "The description is required and must not be blank. Metadata is optional and should be a JSON string containing additional activity information."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Activity created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ActivityResponse.class),
                            examples = @ExampleObject(
                                    value = "{\"id\": 1001, \"activityType\": \"LOGIN\", \"description\": \"User logged in from web\", " +
                                            "\"metadata\": \"{\\\"ip\\\":\\\"192.168.1.10\\\",\\\"device\\\":\\\"Chrome\\\"}\", " +
                                            "\"createdAt\": \"2025-12-10T10:15:30Z\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Validation failure (missing required fields, invalid enum value, or blank description)",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<ActivityResponse> createActivity(
            @Parameter(
                    description = "The unique identifier of the user performing the activity",
                    required = true,
                    example = "123"
            ) @PathVariable Long userId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Activity creation request",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateActivityRequest.class),
                            examples = @ExampleObject(
                                    name = "Login Activity",
                                    value = "{\"activityType\": \"LOGIN\", \"description\": \"User logged in from web\", " +
                                            "\"metadata\": \"{\\\"ip\\\":\\\"192.168.1.10\\\",\\\"device\\\":\\\"Chrome\\\"}\"}"
                            )
                    )
            )
            @Valid @RequestBody CreateActivityRequest request) {

        log.info("\nCreateActivity for userId: {}", userId);
        ActivityResponse response = userActivityService.createActivity(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @DeleteMapping("/activities/{activityId}")
    @Operation(
            summary = "Soft delete an activity",
            description = "Performs a soft delete on an activity by setting the is_deleted flag to true. " +
                    "The activity is not physically removed from the database, allowing for data recovery and audit trails. " +
                    "Soft-deleted activities are automatically excluded from timeline queries."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Activity deleted successfully (No Content)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Activity not found - The specified activity ID does not exist",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - The activity has already been deleted",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<Void> deleteActivity(
            @Parameter(
                    description = "The unique identifier of the activity to delete",
                    required = true,
                    example = "1001"
            ) @PathVariable Long activityId) {

        log.info("Deleting activity for activityId: {}", activityId);
        userActivityService.deleteActivity(activityId);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/users/{userId}/activities/timeline")
    @Operation(
            summary = "Get user activity timeline",
            description = "Retrieves a paginated and sorted timeline of activities for a specific user. " +
                    "Results are sorted by creation date in descending order (newest first). " +
                    "Soft-deleted activities are automatically excluded from the results. " +
                    "Supports pagination with configurable page number and page size."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Timeline retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TimelineResponse.class),
                            examples = @ExampleObject(
                                    value = "{\"userId\": 123, \"page\": 0, \"size\": 20, \"totalElements\": 52, \"totalPages\": 3, " +
                                            "\"activities\": [{\"id\": 1001, \"activityType\": \"LOGIN\", \"description\": \"User logged in from web\", " +
                                            "\"metadata\": \"{\\\"ip\\\":\\\"192.168.1.10\\\"}\", \"createdAt\": \"2025-12-10T10:15:30Z\"}, " +
                                            "{\"id\": 1000, \"activityType\": \"PROFILE_UPDATE\", \"description\": \"User updated profile picture\", " +
                                            "\"metadata\": null, \"createdAt\": \"2025-12-10T09:30:15Z\"}]}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Invalid pagination parameters (negative page or size values)",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<TimelineResponse> getTimeline(
            @Parameter(
                    description = "The unique identifier of the user whose timeline to retrieve",
                    required = true,
                    example = "123"
            ) @PathVariable Long userId,
            @Parameter(
                    description = "Page number (0-indexed). Must be 0 or greater. Default is 0",
                    example = "0"
            )
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page number must be 0 or greater")
            int page,
            @Parameter(
                    description = "Number of items per page. Must be between 1 and 100. Default is 20",
                    example = "20"
            )
            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "Page size must be at least 1")
            @Max(value = 100, message = "Page size cannot exceed 100")
            int size) {

        log.info("\nTimeLine params : UsersID :{} & page: {} && Size: {}", userId, page, size);
        TimelineResponse response = userActivityService.getTimeline(userId, page, size);
        return ResponseEntity.ok(response);
    }
}


