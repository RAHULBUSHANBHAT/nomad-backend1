package com.cts.user.controller;

import com.cts.user.dto.AdminDashboardDto;
import com.cts.user.service.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class AdminDashboardController {

    @Autowired
    private UserServiceImpl userService;

    /**
     * --- THIS IS THE NEW "Admin Dashboard" ENDPOINT ---
     * Orchestrates calls to multiple services to get stats.
     */
    @GetMapping
    public ResponseEntity<AdminDashboardDto> getDashboardStats() {
        log.info("Admin request: getDashboardStats");
        return ResponseEntity.ok(userService.getAdminDashboardStats());
    }
}