package com.hr.employee_management.Dto.attendance;

import com.hr.employee_management.enums.AttendanceStatus;
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
public class AttendanceResponse {
    private Long id;
    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private LocalDate attendanceDate;
    private Instant checkIn;
    private Instant checkOut;
    private AttendanceStatus status;
    private BigDecimal overtimeHours;
    private String remarks;
    private Instant createdAt;
    private Instant updatedAt;
}
