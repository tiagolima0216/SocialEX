package com.tiago_lima.social_media_app.domain.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginUserDto {
    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 30, message = "Username must be between {min} and {max} characters")
    @Pattern(
            regexp = "^[a-z0-9_.]+$",
            message = "Username can only contain lowercase letters, numbers, underscores, and dots"
    )
    private String username;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, max = 100, message = "Password must be between {min} and {max} characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, and one digit"
    )
    private String password;
}
