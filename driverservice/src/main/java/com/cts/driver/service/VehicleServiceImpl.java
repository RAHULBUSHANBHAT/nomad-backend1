package com.cts.driver.service;

import com.cts.driver.dto.VehicleDto;
import com.cts.driver.dto.VehicleTypeCountDto;
import com.cts.driver.exception.ResourceNotFoundException;
import com.cts.driver.mapper.VehicleMapper;
import com.cts.driver.model.Driver;
import com.cts.driver.model.Vehicle;
import com.cts.driver.model.VehicleType;
import com.cts.driver.repository.DriverRepository;
import com.cts.driver.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
    public List<VehicleDto> getVehiclesByDriverUserId(String driverId) {
        Driver driver = driverRepository.findByUserId(driverId)
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

    @Transactional
    public VehicleDto updateVehicle(String userId, String vehicleId, VehicleDto vehicleDto) {
        // First, find the driver to ensure security
        Driver driver = driverRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found"));
        
        // Find the specific vehicle in their list
        Vehicle vehicle = driver.getVehicles().stream()
                .filter(v -> v.getId().equals(vehicleId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found or does not belong to this driver"));

        // Update the vehicle's properties
        // We only allow changing these fields
        vehicle.setVehicleType(vehicleDto.getVehicleType());
        vehicle.setRegistrationNumber(vehicleDto.getRegistrationNumber());
        vehicle.setModel(vehicleDto.getModel());
        vehicle.setPucExpiry(vehicleDto.getPucExpiry());
        vehicle.setInsurancePolicyNumber(vehicleDto.getInsurancePolicyNumber());
        vehicle.setInsuranceExpiry(vehicleDto.getInsuranceExpiry());
        
        // Note: isVerified fields are NOT updated here. They are admin-only.
        
        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        return vehicleMapper.toDto(updatedVehicle);
    }

    @Transactional(readOnly = true)
    public List<VehicleTypeCountDto> getAvailableVehicleCountsByCity(String city) {
        
        // 1. Get the counts that the database *does* have.
        // This list might be incomplete (e.g., no SEDANs found).
        List<VehicleTypeCountDto> dbCounts = vehicleRepository.getAvailableVehicleCountsByCity(city);

        // 2. Convert the DB list into a Map for fast, O(1) lookups.
        // We map: VehicleType -> availableCount
        Map<VehicleType, Long> countMap = dbCounts.stream()
            .collect(Collectors.toMap(
                VehicleTypeCountDto::getVehicleType,
                VehicleTypeCountDto::getAvailableCount
            ));

        // 3. Iterate through ALL possible VehicleType enums.
        return Arrays.stream(VehicleType.values())
            .map(type -> {
                // 4. Get the count from the map, or default to 0L if not found.
                long count = countMap.getOrDefault(type, 0L);
                
                // 5. Create a new DTO with the type and its count (0 or more).
                return new VehicleTypeCountDto(type, count);
            })
            .collect(Collectors.toList());
    }
}