package com.saimone.bvp_software_task.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordRequest {
    @Size(min = 36, max = 36, message = "Invalid confirmation token")
    private String verificationToken;

    @Size(min = 6, message = "The password must be at least 6 characters long")
    @Size(max = 35, message = "Password is too long, please enter up to 35 characters")
    private String newPassword;
}
