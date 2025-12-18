package com.example.demo.controllers;

import com.example.demo.services.JwtService;
import com.example.demo.db.entities.User;
import com.example.demo.db.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;


    @GetMapping("/users")
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @PostMapping("/users")
    public ResponseEntity<TokenResponse> create(@RequestBody CreateUserRequest request) {
        if (request == null
                || isBlank(request.email())
                || isBlank(request.firstName())
                || isBlank(request.lastName())
                || isBlank(request.password())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        Optional<User> existing = userRepository.findByEmail(request.email());
        if (existing.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        String passwordHash = passwordEncoder.encode(request.password());
        User saved = userRepository.save(new User(request.email(), request.firstName(), request.lastName(), passwordHash));
        String token = jwtService.createTokenForUserId(saved.getId());
        return ResponseEntity.ok(new TokenResponse(token));
    }

    @GetMapping("/users/me")
    public ResponseEntity<UserMeResponse> me(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long userId;
        try {
            userId = (Long) authentication.getPrincipal();
        } catch (ClassCastException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return userRepository.findById(userId)
                .map(u -> ResponseEntity.ok(new UserMeResponse(u.getId(), u.getEmail(), u.getFirstName(), u.getLastName(), u.getCreatedAt())))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record CreateUserRequest(String email, String firstName, String lastName, String password) {
    }

    public record TokenResponse(String token) {
    }

    public record UserMeResponse(Long id, String email, String firstName, String lastName, java.time.LocalDateTime createdAt) {
    }
}
