package com.cts.user.dto;

import com.cts.user.model.Status;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserStatusUpdateDto {
    @NotNull(message = "Status cannot be null")
    private Status status;
}