package com.saimone.full_fledged_rest_api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EntryRequest {
    @Email(message = "Enter a valid email address")
    private String email;

    @Size(min = 6, message = "The password must be at least 6 characters long")
    @Size(max = 35, message = "Password is too long, please enter up to 35 characters")
    private String password;
}