package com.example.demo.services;

import com.example.demo.db.entities.User;
import com.example.demo.db.repositories.UserRepository;
import com.example.demo.dto.CreateUserRequest;
import com.example.demo.dto.TokenResponse;
import com.example.demo.dto.UserResponse;
import com.example.demo.mappers.UserMapper;
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
    private final UserMapper userMapper;


    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public TokenResponse create(@RequestBody CreateUserRequest request) {
         Optional<User> existing = userRepository.findByEmail(request.email());
        if (existing.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is present");
        }

        passwordEncoder.encode(request.password());
        User saved = userRepository.save(userMapper.fromRequest(request));
        String token = jwtService.createTokenForUserId(saved.getId());
        return new TokenResponse(token);
    }


    public UserResponse me(Long userId) {
        return userRepository.findById(userId).map(userMapper::toResponse).orElseThrow();
    }

}
