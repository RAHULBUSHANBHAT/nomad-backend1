package com.cts.user.service;

import java.util.List; // <-- ADD

import org.springframework.beans.factory.annotation.Autowired; // <-- ADD
import org.springframework.beans.factory.annotation.Value; // <-- ADD
import org.springframework.data.domain.Page; // <-- ADD
import org.springframework.data.domain.Pageable; // <-- ADD
import org.springframework.kafka.core.KafkaTemplate; // <-- ADD
import org.springframework.security.crypto.password.PasswordEncoder; // <-- ADD
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cts.user.client.DriverClient;
import com.cts.user.client.WalletClient;
import com.cts.user.dto.AdminDashboardDto;
import com.cts.user.dto.RegisterUserDto;
import com.cts.user.dto.TransactionDto;
import com.cts.user.dto.UpdateUserDto;
import com.cts.user.dto.UserDto;
import com.cts.user.dto.UserStatusUpdateDto;
import com.cts.user.dto.WalletDto;
import com.cts.user.dto.kafka.UserEventDto;
import com.cts.user.exception.DuplicateResourceException;
import com.cts.user.exception.ResourceNotFoundException;
import com.cts.user.mapper.UserMapper;
import com.cts.user.model.Role;
import com.cts.user.model.User;
import com.cts.user.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserServiceImpl {


    @Autowired private DriverClient driverClient;
    @Autowired private WalletClient walletClient;

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

    @Value("${app.company-wallet-user-id}") // <-- We need this ID from wallet's yml
    private String companyWalletUserId;
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
    
    @Transactional
    public UserDto updateUserStatus(String userId, UserStatusUpdateDto dto) {
        log.info("Admin is updating status for user {} to {}", userId, dto.getStatus());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        user.setStatus(dto.getStatus()); // Set to ACTIVE or SUSPENDED
        User savedUser = userRepository.save(user);
        
        // We should also produce a "USER_UPDATED" event
        // kafkaTemplate.send(...) 
        
        return userMapper.toUserDto(savedUser);
    }

    public AdminDashboardDto getAdminDashboardStats() {
        log.info("Fetching admin dashboard stats...");

        // 1. Fetch counts (these are fast, run in parallel)
        long totalRiders = userRepository.countByRole(Role.RIDER);
        long totalDrivers = driverClient.getDriverCount(); // Feign call

        // 2. Fetch financials (these are fast, run in parallel)
        WalletDto companyWallet = walletClient.getWalletByUserIdInternal(companyWalletUserId); // Feign call
        List<TransactionDto> latestTx = walletClient.getLatestTransactions(); // Feign call

        // 3. Build and return the DTO
        return AdminDashboardDto.builder()
                .totalRiders(totalRiders)
                .totalDrivers(totalDrivers)
                .companyWalletBalance(companyWallet.getBalance())
                .latestTransactions(latestTx)
                .build();
    }

    public Page<UserDto> getAllRiders(Pageable pageable) {
        log.info("Admin request: Fetching all RIDERs, page {} of size {}", pageable.getPageNumber(), pageable.getPageSize());
        // Call the new repository method
        return userRepository.findByRole(Role.RIDER, pageable)
                .map(userMapper::toUserDto);
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
    @Transactional
    public void addRating(String userId, int newRating) {
        log.info("Adding new rating of {} for user ID: {}", newRating, userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        // This is a robust average calculation
        int oldTotalRatings = user.getTotalRatings();
        float oldSum = user.getRating() * oldTotalRatings;
        
        int newTotalRatings = oldTotalRatings + 1;
        float newSum = oldSum + newRating;
        
        user.setTotalRatings(newTotalRatings);
        user.setRating(newSum / newTotalRatings);
        
        userRepository.save(user);
        log.info("User {} new average rating is: {}", userId, user.getRating());
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