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
        dto.setPucExpiry(entity.getPucExpiry());
        dto.setInsurancePolicyNumber(entity.getInsurancePolicyNumber());
        dto.setInsuranceExpiry(entity.getInsuranceExpiry());
        dto.setPucNumber(entity.getPucNumber());
        dto.setPucExpiry(entity.getPucExpiry());
        dto.setPucVerified(entity.isPucVerified());
        dto.setInsurancePolicyNumber(entity.getInsurancePolicyNumber());
        dto.setInsuranceVerified(entity.isInsuranceVerified());
        dto.setInsuranceExpiry(entity.getInsuranceExpiry());
        dto.setRcVerified(entity.isRcVerified());
        return dto;
    }

    public Vehicle toEntity(VehicleDto dto) {
        Vehicle entity = new Vehicle();
        // We don't map ID, it's generated
        entity.setVehicleType(dto.getVehicleType());
        entity.setRegistrationNumber(dto.getRegistrationNumber());
        entity.setModel(dto.getModel());
        entity.setPucExpiry(dto.getPucExpiry());
        entity.setManufacturer(dto.getManufacturer());
        entity.setModel(dto.getModel());
        entity.setColor(dto.getColor());
        entity.setInsurancePolicyNumber(dto.getInsurancePolicyNumber());
        entity.setInsuranceExpiry(dto.getInsuranceExpiry());
        entity.setPucNumber(dto.getPucNumber());
        entity.setPucVerified(dto.isPucVerified());
        entity.setInsurancePolicyNumber(dto.getInsurancePolicyNumber());
        entity.setInsuranceVerified(dto.isInsuranceVerified());
        return entity;
    }
}