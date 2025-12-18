package com.example.demo.services;

import com.example.demo.db.entities.User;
import com.example.demo.db.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;


    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public TokenResponse create(@RequestBody CreateUserRequest request) {
        if (request == null
                || isBlank(request.email())
                || isBlank(request.firstName())
                || isBlank(request.lastName())
                || isBlank(request.password())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email is required");
        }

        Optional<User> existing = userRepository.findByEmail(request.email());
        if (existing.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is present");
        }

        String passwordHash = passwordEncoder.encode(request.password());
        User saved = userRepository.save(new User(request.email(), request.firstName(), request.lastName(), passwordHash));
        String token = jwtService.createTokenForUserId(saved.getId());
        return new TokenResponse(token);
    }


    public User me(Long userId) {

        return userRepository.findById(userId).orElseThrow();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record CreateUserRequest(String email, String firstName, String lastName, String password) {
    }

    public record TokenResponse(String token) {
    }
}
