package com.api.auth_service.api.models.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long userId;
    private String login;
    private String email;
    private String firstName;
    private String lastName;
    private String middleName;
    private Long teamId;
    private Long positionId;
    private Boolean isActive;
    private String createdAt;
    private String lastModifiedAt;
}