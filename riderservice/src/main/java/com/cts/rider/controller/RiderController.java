package com.cts.rider.controller;

import com.cts.rider.dto.RiderAccountDto;
import com.cts.rider.dto.UpdateRiderDto;
import com.cts.rider.service.RiderServiceImpl;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/riders")
@PreAuthorize("hasRole('RIDER')")
@Slf4j
public class RiderController {

    @Autowired
    private RiderServiceImpl riderService;

    @GetMapping("/me/account")
    public ResponseEntity<RiderAccountDto> getMyAccountDetails(Authentication authentication) {
        log.info("Rider request: getMyAccountDetails");
        return ResponseEntity.ok(riderService.getMyAccountDetails(authentication));
    }

    @PutMapping("/me/account")
    public ResponseEntity<?> updateMyAccountDetails(Authentication authentication, @Valid @RequestBody UpdateRiderDto updateDto) {
        log.info("Rider request: updateMyAccountDetails");
        return ResponseEntity.ok(riderService.updateMyAccountDetails(authentication, updateDto));
    }
}