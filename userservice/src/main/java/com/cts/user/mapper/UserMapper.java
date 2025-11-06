package com.cts.user.mapper;

import com.cts.user.dto.RegisterUserDto;
import com.cts.user.dto.UserDto;
import com.cts.user.model.User;
import com.cts.user.model.Status;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toUser(RegisterUserDto dto) {
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setRole(dto.getRole());
        user.setStatus(Status.PENDING); 
        return user;
    }

    public UserDto toUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setCity(user.getCity());
        dto.setState(user.getState());
        dto.setEmailVerified(user.isEmailVerified());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        dto.setRating(user.getRating());
        dto.setTotalRatings(user.getTotalRatings());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}