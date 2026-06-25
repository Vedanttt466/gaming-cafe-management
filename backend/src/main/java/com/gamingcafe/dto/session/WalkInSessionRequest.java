package com.gamingcafe.dto.session;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class WalkInSessionRequest {

    @NotBlank(message = "Walk-in customer name is required")
    private String name;

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be a 10-digit number")
    private String phone; // optional
}
