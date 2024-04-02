package com.saimone.bvp_software_task.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserEntryRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;
}