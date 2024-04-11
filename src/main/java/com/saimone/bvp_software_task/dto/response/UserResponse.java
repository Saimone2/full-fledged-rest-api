package com.saimone.bvp_software_task.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {
    @JsonProperty("id")
    private Long id;
    @JsonProperty("email")
    private String email;
    @JsonProperty("enabled")
    private Boolean enabled;
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    public UserResponse(Long id, String email, Boolean enabled, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.enabled = enabled;
        this.createdAt = createdAt;
    }
}