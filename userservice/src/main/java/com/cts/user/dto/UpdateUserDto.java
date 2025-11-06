package com.cts.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserDto {
    
    @Size(min = 1, max = 50)
    private String firstName;
    
    @Size(min = 1, max = 50)
    private String lastName;
    
    @Size(min = 1, max = 50)
    private String city;
    
    @Size(min = 1, max = 50)
    private String state;
}