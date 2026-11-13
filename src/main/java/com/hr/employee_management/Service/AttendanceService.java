package com.hr.employee_management.Service;

import com.hr.employee_management.Dto.attendance.AttendanceRequest;
import com.hr.employee_management.Dto.attendance.AttendanceResponse;
import com.hr.employee_management.Dto.attendance.AttendanceSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface AttendanceService {
    AttendanceResponse recordAttendance(AttendanceRequest request);
    Page<AttendanceResponse> getAllAttendance(LocalDate date, LocalDate from, LocalDate to, Pageable pageable);
    AttendanceResponse getAttendanceById(Long id);
    AttendanceResponse updateAttendance(Long id, AttendanceRequest request);
    Page<AttendanceResponse> getAttendanceByEmployee(Long employeeId, Pageable pageable);
    AttendanceSummaryResponse getAttendanceSummary(int month, int year);
}
