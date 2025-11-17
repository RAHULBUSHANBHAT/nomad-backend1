package com.cts.user.controller;

import com.cts.user.dto.RegisterUserDto;
import com.cts.user.dto.UpdateUserDto;
import com.cts.user.dto.UserDto;
import com.cts.user.dto.UserStatusUpdateDto;
import com.cts.user.service.UserServiceImpl;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users") // Aligns with the *public* gateway route
@Slf4j
public class UserController {

    @Autowired
    private UserServiceImpl userService;

    // Gets the role *without* the "ROLE_" prefix
    private String getUserRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.replace("ROLE_", ""))
                .orElse("");
    }

    /**
     * Public endpoint for user registration.
     */
    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@Valid @RequestBody RegisterUserDto registerUserDto) {
        log.info("Received registration request for email: {}", registerUserDto.getEmail());
        UserDto newUser = userService.registerUser(registerUserDto);
        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }

    /**
     * Get the details of the *currently logged-in* user.
     * Layer 3 Security: @PreAuthorize("isAuthenticated()")
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDto> getCurrentUser(Authentication authentication) {
        String email = authentication.getName(); 
        log.info("Fetching profile for /me, authenticated user: {}", email);
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    /**
     * Updates the details of the *currently logged-in* user.
     * Layer 3 Security: @PreAuthorize("isAuthenticated()")
     */
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDto> updateCurrentUser(Authentication authentication, @Valid @RequestBody UpdateUserDto updateUserDto) {
        String userId = (String) authentication.getDetails();
        log.info("Updating profile for /me, authenticated user ID: {}", userId);
        UserDto updatedUser = userService.updateUser(userId, updateUserDto);
        return ResponseEntity.ok(updatedUser);
    }

    // --- ADMIN ENDPOINTS (as requested) ---

    /**
     * Admin-only: Get a paginated list of all users.
     * Layer 3 Security: @PreAuthorize("hasRole('ADMIN')")
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserDto>> getAllUsers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Admin request: Received for getAllUsers, page {} size {}", pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }
    @PutMapping("/admin/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> updateUserStatus(
            @PathVariable String id, 
            @Valid @RequestBody UserStatusUpdateDto dto) {
        
        log.info("Admin request: Update status for user {}", id);
        return ResponseEntity.ok(userService.updateUserStatus(id, dto));
    }

    /**
     * Admin-only: Get any user by their ID.
     * Layer 3 Security: @PreAuthorize("hasRole('ADMIN')")
     */
    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> getUserByIdForAdmin(@PathVariable String id) {
        log.info("Admin request: Fetching user by ID: {}", id);
        return ResponseEntity.ok(userService.getUserById(id));
    }

    /**
     * An example of Fine-Grained, data-level security.
     * Only an ADMIN, or the user *themselves*, can access this.
     */
    @GetMapping("/{id}/profile")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwner(authentication, #id)")
    public ResponseEntity<UserDto> getUserProfile(@PathVariable String id) {
        log.info("Fetching user profile for ID: {}", id);
        return ResponseEntity.ok(userService.getUserById(id));
    }
    @GetMapping("/admin/riders")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserDto>> getAllRiders(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String filterType,   // Expected: "EMAIL" or "PHONE"
            @RequestParam(required = false) String searchContent // Expected: "john@gmail.com" or "9876543210"
    ) {
        log.info("Admin request: getAllRiders with filterType: {} and content: {}", filterType, searchContent);
        
        return ResponseEntity.ok(userService.getAllRiders(pageable, filterType, searchContent));
    }


}