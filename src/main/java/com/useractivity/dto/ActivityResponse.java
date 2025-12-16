package com.useractivity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO containing activity information")
public class ActivityResponse {

    @Schema(description = "Unique identifier of the activity", example = "1001")
    private Long id;

    @Schema(description = "Type of activity", example = "LOGIN")
    private String activityType;

    @Schema(description = "Description of the activity", example = "User logged in from web")
    private String description;

    @Schema(description = "JSON string containing additional metadata",
            example = "{\"ip\":\"192.168.1.10\",\"device\":\"Chrome\"}", nullable = true)
    private String metadata;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    @Schema(description = "Timestamp when the activity was created (ISO-8601 format)",
            example = "2025-12-10T10:15:30Z")
    private LocalDateTime createdAt;

}
