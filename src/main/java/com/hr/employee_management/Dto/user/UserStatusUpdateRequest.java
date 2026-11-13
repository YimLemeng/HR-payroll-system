package com.hr.employee_management.Dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserStatusUpdateRequest {

    @NotNull(message = "Active status is required")
    private Boolean active;
}
