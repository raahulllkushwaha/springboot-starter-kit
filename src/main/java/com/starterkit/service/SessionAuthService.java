package com.starterkit.service;

import com.starterkit.dto.request.LoginRequest;
import com.starterkit.dto.request.RegisterRequest;
import com.starterkit.dto.response.UserResponse;
import com.starterkit.entity.Role;
import com.starterkit.entity.User;
import com.starterkit.exception.DuplicateResourceException;
import com.starterkit.exception.ResourceNotFoundException;
import com.starterkit.repository.RoleRepository;
import com.starterkit.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@Profile("session")
@RequiredArgsConstructor
@Slf4j
public class SessionAuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    private final SecurityContextHolderStrategy securityContextHolderStrategy =
            SecurityContextHolder.getContextHolderStrategy();

    @Transactional
    public UserResponse register(RegisterRequest request,
                                 HttpServletRequest httpRequest,
                                 HttpServletResponse httpResponse) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", "ROLE_USER"));

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(userRole))
                .build();

        userRepository.save(user);
        log.info("New user registered: {}", user.getEmail());

        // Auto-login after register using Spring Security's flow
        authenticateAndSaveContext(request.getEmail(), request.getPassword(), httpRequest, httpResponse);

        return UserResponse.from(user);
    }

    public UserResponse login(LoginRequest request,
                              HttpServletRequest httpRequest,
                              HttpServletResponse httpResponse) {

        authenticateAndSaveContext(request.getEmail(), request.getPassword(), httpRequest, httpResponse);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        log.info("User logged in: {}", request.getEmail());
        return UserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return UserResponse.from(user);
    }


    private void authenticateAndSaveContext(String email, String password,
                                            HttpServletRequest request,
                                            HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        // Save to SecurityContextHolder
        SecurityContext context = securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(authentication);
        securityContextHolderStrategy.setContext(context);

        // Persist to JDBC session store (Spring Session)
        // This automatically sets the SESSION cookie in the response
        securityContextRepository.saveContext(context, request, response);
    }
}
