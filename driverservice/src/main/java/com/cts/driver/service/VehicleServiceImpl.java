package com.cts.driver.service;

import com.cts.driver.dto.VehicleDto;
import com.cts.driver.exception.ResourceNotFoundException;
import com.cts.driver.mapper.VehicleMapper;
import com.cts.driver.model.Driver;
import com.cts.driver.model.Vehicle;
import com.cts.driver.repository.DriverRepository;
import com.cts.driver.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VehicleServiceImpl {

    @Autowired private DriverRepository driverRepository;
    @Autowired private VehicleRepository vehicleRepository; // Not strictly needed for these ops
    @Autowired private VehicleMapper vehicleMapper;

    @Transactional
    public VehicleDto addVehicle(String userId, VehicleDto vehicleDto) {
        Driver driver = driverRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found"));
        
        Vehicle vehicle = vehicleMapper.toEntity(vehicleDto);
        driver.getVehicles().add(vehicle); // Add to the list
        driverRepository.save(driver); // Cascade will save the vehicle
        
        // Get the just-saved vehicle to return its generated ID
        Vehicle savedVehicle = driver.getVehicles().get(driver.getVehicles().size() - 1);
        return vehicleMapper.toDto(savedVehicle);
    }

    @Transactional(readOnly = true)
    public List<VehicleDto> getVehiclesByUserId(String userId) {
        Driver driver = driverRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found"));
                
        return driver.getVehicles().stream()
                .map(vehicleMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteVehicle(String userId, String vehicleId) {
        Driver driver = driverRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found"));
        
        boolean removed = driver.getVehicles().removeIf(v -> v.getId().equals(vehicleId));
        if (!removed) {
            throw new ResourceNotFoundException("Vehicle not found or does not belong to this driver");
        }
        driverRepository.save(driver); // orphanRemoval=true handles delete
    }
}