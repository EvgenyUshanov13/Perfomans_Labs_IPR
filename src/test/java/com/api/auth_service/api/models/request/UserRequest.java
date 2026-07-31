package com.api.auth_service.api.models.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {
    private String login;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String middleName;
    private Long teamId;
    private Long positionId;
}