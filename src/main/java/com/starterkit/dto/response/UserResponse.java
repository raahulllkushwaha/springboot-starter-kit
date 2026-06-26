package com.starterkit.dto.response;

import com.starterkit.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Builder
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private Set<String> roles;
    private boolean enabled;
    private LocalDateTime createdAt;

    public static UserResponse from(User user) {
        return UserResponse.builder()
            .id(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .roles(user.getRoles().stream().map(r -> r.getName()).collect(Collectors.toSet()))
            .enabled(user.isEnabled())
            .createdAt(user.getCreatedAt())
            .build();
    }
}
