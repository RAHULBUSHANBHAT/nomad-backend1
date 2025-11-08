package com.cts.driver.mapper;

import com.cts.driver.dto.VehicleDto;
import com.cts.driver.model.Vehicle;
import org.springframework.stereotype.Component;

@Component
public class VehicleMapper {
    
    public VehicleDto toDto(Vehicle entity) {
        VehicleDto dto = new VehicleDto();
        dto.setId(entity.getId());
        dto.setVehicleType(entity.getVehicleType());
        dto.setRegistrationNumber(entity.getRegistrationNumber());
        dto.setModel(entity.getModel());
        dto.setRcNumber(entity.getRcNumber());
        dto.setPucExpiry(entity.getPucExpiry());
        dto.setInsurancePolicyNumber(entity.getInsurancePolicyNumber());
        dto.setInsuranceExpiry(entity.getInsuranceExpiry());
        return dto;
    }

    public Vehicle toEntity(VehicleDto dto) {
        Vehicle entity = new Vehicle();
        // We don't map ID, it's generated
        entity.setVehicleType(dto.getVehicleType());
        entity.setRegistrationNumber(dto.getRegistrationNumber());
        entity.setModel(dto.getModel());
        entity.setRcNumber(dto.getRcNumber());
        entity.setPucExpiry(dto.getPucExpiry());
        entity.setInsurancePolicyNumber(dto.getInsurancePolicyNumber());
        entity.setInsuranceExpiry(dto.getInsuranceExpiry());
        return entity;
    }
}