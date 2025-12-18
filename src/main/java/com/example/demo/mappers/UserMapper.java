package com.example.demo.mappers;

import com.example.demo.db.entities.User;
import com.example.demo.dto.CreateUserRequest;
import com.example.demo.dto.UserResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(User user);
    User fromRequest(CreateUserRequest request);
}
