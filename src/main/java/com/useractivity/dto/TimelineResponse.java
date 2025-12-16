package com.useractivity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO containing paginated timeline of user activities")
public class TimelineResponse {

    @Schema(description = "The user ID for which the timeline is retrieved", example = "123")
    private Long userId;

    @Schema(description = "Current page number (0-indexed)", example = "0")
    private Integer page;

    @Schema(description = "Number of items per page", example = "20")
    private Integer size;

    @Schema(description = "Total number of activities (excluding soft-deleted)", example = "52")
    private Long totalElements;

    @Schema(description = "Total number of pages", example = "3")
    private Integer totalPages;

    @Schema(description = "List of activities for the current page")
    private List<ActivityResponse> activities;

}
