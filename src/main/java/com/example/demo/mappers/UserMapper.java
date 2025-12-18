package com.example.demo.mappers;

import com.example.demo.db.entities.User;
import com.example.demo.dto.CreateUserRequest;
import com.example.demo.dto.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "passwordHash", source = "passwordHash")
    User fromRequest(CreateUserRequest request, String passwordHash);
}
