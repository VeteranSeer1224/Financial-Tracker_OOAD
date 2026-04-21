package com.finance.tracker.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InitiateOtpRequest {
    @Email
    @NotBlank
    private String email;
}
