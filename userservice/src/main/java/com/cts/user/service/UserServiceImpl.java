package com.cts.user.service;

import com.cts.user.dto.RegisterUserDto;
import com.cts.user.dto.UpdateUserDto;
import com.cts.user.dto.UserDto;
import com.cts.user.dto.kafka.UserEventDto;
import com.cts.user.exception.DuplicateResourceException;
import com.cts.user.exception.ResourceNotFoundException;
import com.cts.user.mapper.UserMapper;
import com.cts.user.model.User;
import com.cts.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class UserServiceImpl {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder; // For hashing

    @Autowired
    private KafkaTemplate<String, UserEventDto> kafkaTemplate; // For producing

    @Value("${app.kafka.topics.user-events}")
    private String userEventsTopic;

    @Transactional
    public UserDto registerUser(RegisterUserDto registerUserDto) {
        log.info("Attempting to register new user: {}", registerUserDto.getEmail());

        // 1. Check for duplicates
        if (userRepository.existsByEmail(registerUserDto.getEmail())) {
            throw new DuplicateResourceException("Email already in use: " + registerUserDto.getEmail());
        }
        if (userRepository.existsByPhoneNumber(registerUserDto.getPhoneNumber())) {
            throw new DuplicateResourceException("Phone number already in use: " + registerUserDto.getPhoneNumber());
        }

        // 2. Map DTO to Entity and hash password
        User user = userMapper.toUser(registerUserDto);
        user.setPassword(passwordEncoder.encode(registerUserDto.getPassword()));

        // 3. Save the new user to our local MySQL database
        User savedUser = userRepository.save(user);
        log.info("User saved to database with ID: {}", savedUser.getId());

        // 4. Create the Kafka event DTO
        UserEventDto eventDto = new UserEventDto(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getPassword(), // Send the HASHED password
                savedUser.getRole().name(),
                "USER_CREATED"
        );

        // 5. Send the event to the Kafka topic
        try {
            log.debug("Sending Kafka event: id={}, type={}, email={}, role={}", 
                     eventDto.getUserId(), eventDto.getEventType(), 
                     eventDto.getEmail(), eventDto.getRole());
            kafkaTemplate.send(userEventsTopic, savedUser.getId(), eventDto);
            log.info("Sent 'USER_CREATED' event to Kafka topic: {}", userEventsTopic);
        } catch (Exception e) {
            log.error("Failed to send 'USER_CREATED' event to Kafka. Rolling back transaction.", e);
            throw new RuntimeException("Failed to send user creation event, registration rolled back.", e);
        }

        return userMapper.toUserDto(savedUser);
    }

    public UserDto getUserById(String id) {
        log.debug("Fetching user by ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return userMapper.toUserDto(user);
    }
    
    public UserDto getUserByEmail(String email) {
        log.debug("Fetching user by email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return userMapper.toUserDto(user);
    }

    @Transactional
    public UserDto updateUser(String userId, UpdateUserDto updateUserDto) {
        log.info("Updating profile for user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setFirstName(updateUserDto.getFirstName());
        user.setLastName(updateUserDto.getLastName());
        user.setCity(updateUserDto.getCity());
        user.setState(updateUserDto.getState());

        User updatedUser = userRepository.save(user);
        return userMapper.toUserDto(updatedUser);
    }
    
    /**
     * Admin-only function to get all users with pagination.
     * As you requested.
     */
    public Page<UserDto> getAllUsers(Pageable pageable) {
        log.info("Admin request: Fetching all users, page {} of size {}", pageable.getPageNumber(), pageable.getPageSize());
        // Spring Data JPA handles the pagination query automatically
        return userRepository.findAll(pageable)
                .map(userMapper::toUserDto);
    }
}