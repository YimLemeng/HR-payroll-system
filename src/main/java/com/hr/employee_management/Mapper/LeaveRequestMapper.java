package com.hr.employee_management.Mapper;

import com.hr.employee_management.Dto.leave.LeaveRequestDto;
import com.hr.employee_management.Dto.leave.LeaveResponse;
import com.hr.employee_management.Entity.Employee;
import com.hr.employee_management.Entity.LeaveRequest;
import com.hr.employee_management.enums.LeaveStatus;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;

@Component
public class LeaveRequestMapper {
    public LeaveRequest toEntity(LeaveRequestDto request, Employee employee) {
        if (request == null) return null;
        java.time.Instant now = java.time.Instant.now();
        return LeaveRequest.builder()
                .employee(employee)
                .leaveType(request.getLeaveType())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .reason(request.getReason().trim())
                .status(LeaveStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public LeaveResponse toResponse(LeaveRequest entity) {
        if (entity == null) return null;

        Employee emp = entity.getEmployee();
        long totalDays = ChronoUnit.DAYS.between(entity.getStartDate(), entity.getEndDate()) + 1;
        return LeaveResponse.builder()
                .id(entity.getId())
                .employeeId(emp != null ? emp.getId() : null)
                .employeeCode(emp != null ? emp.getEmployeeCode() : null)
                .employeeName(emp != null ? (emp.getFirstName() + " " + emp.getLastName()) : null)
                .leaveType(entity.getLeaveType())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .totalDays(totalDays)
                .reason(entity.getReason())
                .status(entity.getStatus())
                .reviewedByUsername(entity.getReviewedBy() != null ? entity.getReviewedBy().getUsername() : null)
                .reviewedAt(entity.getReviewedAt())
                .reviewComment(entity.getReviewComment())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
