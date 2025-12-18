package com.example.demo.controllers;

import com.example.demo.db.entities.User;
import com.example.demo.config.AuthPrincipal;
import com.example.demo.dto.CreateUserRequest;
import com.example.demo.dto.TokenResponse;
import com.example.demo.dto.UserResponse;
import com.example.demo.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/users")
@RestController()
@RequiredArgsConstructor
@Tag(name = "Users")
public class UserController {

    private final UserService userService;


    @GetMapping()
    @Operation(summary = "List users")
    public ResponseEntity<List<UserResponse>> getUsers() {
        return ResponseEntity.ok(userService.getUsers());
    }


    @PostMapping()
    @Operation(summary = "Create user (signup)")
    public ResponseEntity<TokenResponse> create(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(userService.create(request));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserResponse> me(Authentication authentication) {
        AuthPrincipal principal = (AuthPrincipal) authentication.getPrincipal();
        Long userId = principal.userId();
        return ResponseEntity.ok(userService.me(userId));
    }
}
