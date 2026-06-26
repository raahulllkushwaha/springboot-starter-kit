package com.starterkit.controller;

import com.starterkit.dto.request.LoginRequest;
import com.starterkit.dto.request.RegisterRequest;
import com.starterkit.dto.response.ApiResponse;
import com.starterkit.dto.response.UserResponse;
import com.starterkit.service.SessionAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Profile("session")
@Tag(name = "Authentication (Session)", description = "Session-based auth using Spring Security")
public class SessionAuthController {

    private final SessionAuthService sessionAuthService;
    private final SecurityContextRepository securityContextRepository;


    @PostMapping("/register")
    @Operation(summary = "Register new user (session-based)")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        UserResponse user = sessionAuthService.register(request, httpRequest, httpResponse);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registered successfully", user));
    }


    @PostMapping("/login")
    @Operation(summary = "Login (session-based)")
    public ResponseEntity<ApiResponse<UserResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        UserResponse user = sessionAuthService.login(request, httpRequest, httpResponse);
        return ResponseEntity.ok(ApiResponse.success("Login successful", user));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current session user")
    public ResponseEntity<ApiResponse<UserResponse>> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(ApiResponse.success(
                sessionAuthService.getCurrentUser(auth.getName())
        ));
    }
}
