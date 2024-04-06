package com.saimone.bvp_software_task.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EntryRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;
}
