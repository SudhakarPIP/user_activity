package com.useractivity.dto;

import com.useractivity.enums.ActivityType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for creating a new user activity")
public class CreateActivityRequest {

    @NotNull(message = "activityType is required")
    @Schema(description = "Type of activity", requiredMode = Schema.RequiredMode.REQUIRED, example = "LOGIN",
            allowableValues = {"LOGIN", "LOGOUT", "PASSWORD_CHANGE", "PROFILE_UPDATE"})
    private ActivityType activityType;

    @NotBlank(message = "description is required")
    @Schema(description = "Human-readable description of the activity", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "User logged in from web")
    private String description;

    @Schema(description = "Optional JSON string containing additional metadata about the activity",
            example = "{\"ip\":\"192.168.1.10\",\"device\":\"Chrome\"}", nullable = true)
    private String metadata; // JSON string

}


