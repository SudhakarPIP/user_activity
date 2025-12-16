package com.useractivity.dto;

import com.useractivity.enums.ActivityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateActivityRequest {
    
    @NotNull(message = "activityType is required")
    private ActivityType activityType;
    
    @NotBlank(message = "description is required")
    private String description;
    
    private String metadata; // JSON string

}


