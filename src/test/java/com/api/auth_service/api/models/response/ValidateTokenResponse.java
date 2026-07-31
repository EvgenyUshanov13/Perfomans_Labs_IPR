package com.api.auth_service.api.models.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateTokenResponse {
    private Boolean valid;
    private String error;
    private Long userId;
    private String login;
    private Long teamId;
}