package com.hr.employee_management.Mapper;

import com.hr.employee_management.Dto.attendance.AttendanceRequest;
import com.hr.employee_management.Dto.attendance.AttendanceResponse;
import com.hr.employee_management.Entity.Attendance;
import com.hr.employee_management.Entity.Employee;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class AttendanceMapper {
    public Attendance toEntity(AttendanceRequest request, Employee employee) {
        if (request == null) return null;
        return Attendance.builder()
                .employee(employee)
                .attendanceDate(request.getAttendanceDate())
                .checkIn(request.getCheckIn())
                .checkOut(request.getCheckOut())
                .status(request.getStatus())
                .overtimeHours(request.getOvertimeHours() != null ? request.getOvertimeHours() : BigDecimal.ZERO)
                .remarks(request.getRemarks())
                .build();
    }

    public AttendanceResponse toResponse(Attendance a) {
        if (a == null) return null;

        Employee emp = a.getEmployee();
        return AttendanceResponse.builder()
                .id(a.getId())
                .employeeId(emp != null ? emp.getId() : null)
                .employeeCode(emp != null ? emp.getEmployeeCode() : null)
                .employeeName(emp != null ? (emp.getFirstName() + " " + emp.getLastName()) : null)
                .attendanceDate(a.getAttendanceDate())
                .checkIn(a.getCheckIn())
                .checkOut(a.getCheckOut())
                .status(a.getStatus())
                .overtimeHours(a.getOvertimeHours())
                .remarks(a.getRemarks())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
