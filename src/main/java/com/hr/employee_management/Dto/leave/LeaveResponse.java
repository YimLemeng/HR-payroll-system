package com.hr.employee_management.Dto.leave;

import com.hr.employee_management.enums.LeaveStatus;
import com.hr.employee_management.enums.LeaveType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LeaveResponse {
    private Long id;
    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private LeaveType leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private long totalDays;
    private String reason;
    private LeaveStatus status;
    private String reviewedByUsername;
    private Instant reviewedAt;
    private String reviewComment;
    private Instant createdAt;
    private Instant updatedAt;
}
