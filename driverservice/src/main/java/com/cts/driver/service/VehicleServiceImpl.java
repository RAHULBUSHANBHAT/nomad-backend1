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
    @Autowired private VehicleRepository vehicleRepository;
    @Autowired private VehicleMapper vehicleMapper;

    @Transactional
    public VehicleDto addVehicle(String userId, VehicleDto vehicleDto) {
        Driver driver = driverRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found"));
        
        Vehicle vehicle = vehicleMapper.toEntity(vehicleDto);
        driver.getVehicles().add(vehicle);
        driverRepository.save(driver);
        
        Vehicle savedVehicle = driver.getVehicles().get(driver.getVehicles().size() - 1);
        return vehicleMapper.toDto(savedVehicle);
    }

    @Transactional(readOnly = true)
    public List<VehicleDto> getVehiclesByDriverUserId(String driverUserId) {
        Driver driver = driverRepository.findByUserId(driverUserId)
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
        driverRepository.save(driver);
    }

    @Transactional
    public VehicleDto updateVehicle(String userId, String vehicleId, VehicleDto vehicleDto) {
        Driver driver = driverRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found"));
        
        Vehicle vehicle = driver.getVehicles().stream()
                .filter(v -> v.getId().equals(vehicleId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found or does not belong to this driver"));

        vehicle.setVehicleType(vehicleDto.getVehicleType());
        vehicle.setRegistrationNumber(vehicleDto.getRegistrationNumber());
        vehicle.setModel(vehicleDto.getModel());
        vehicle.setPucExpiry(vehicleDto.getPucExpiry());
        vehicle.setInsurancePolicyNumber(vehicleDto.getInsurancePolicyNumber());
        vehicle.setInsuranceExpiry(vehicleDto.getInsuranceExpiry());
        
        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        return vehicleMapper.toDto(updatedVehicle);
    }

    @Transactional(readOnly = true)
    public List<VehicleTypeCountDto> getAvailableVehicleCountsByCity(String city) {
        List<VehicleTypeCountDto> dbCounts = vehicleRepository.getAvailableVehicleCountsByCity(city);

        Map<VehicleType, Long> countMap = dbCounts.stream()
            .collect(Collectors.toMap(
                VehicleTypeCountDto::getVehicleType,
                VehicleTypeCountDto::getAvailableCount
            ));

        return Arrays.stream(VehicleType.values())
            .map(type -> {
                long count = countMap.getOrDefault(type, 0L);
                
                return new VehicleTypeCountDto(type, count);
            })
            .collect(Collectors.toList());
    }
}