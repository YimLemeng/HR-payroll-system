package com.hr.employee_management.Dto.attendance;

import com.hr.employee_management.enums.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttendanceRequest {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "Attendance date is required")
    private LocalDate attendanceDate;

    private Instant checkIn;

    private Instant checkOut;

    @NotNull(message = "Attendance status is required")
    private AttendanceStatus status;

    @PositiveOrZero(message = "Overtime hours must be zero or positive")
    private BigDecimal overtimeHours;

    @Size(max = 255, message = "Remarks must not exceed 255 characters")
    private String remarks;
}
