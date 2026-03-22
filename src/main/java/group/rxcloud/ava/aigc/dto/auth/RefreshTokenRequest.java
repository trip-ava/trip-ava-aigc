package group.rxcloud.ava.aigc.dto.auth;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * Refresh token request DTO
 */
@Data
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
