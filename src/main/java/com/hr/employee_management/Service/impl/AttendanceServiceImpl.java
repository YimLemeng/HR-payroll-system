package com.hr.employee_management.Service.impl;

import com.hr.employee_management.Dto.attendance.AttendanceRequest;
import com.hr.employee_management.Dto.attendance.AttendanceResponse;
import com.hr.employee_management.Dto.attendance.AttendanceSummaryResponse;
import com.hr.employee_management.Entity.Attendance;
import com.hr.employee_management.Entity.Employee;
import com.hr.employee_management.Exception.BusinessRuleException;
import com.hr.employee_management.Exception.DuplicateResourceException;
import com.hr.employee_management.Exception.ResourceNotFoundException;
import com.hr.employee_management.Mapper.AttendanceMapper;
import com.hr.employee_management.Repository.AttendanceRepository;
import com.hr.employee_management.Repository.EmployeeRepository;
import com.hr.employee_management.Service.AttendanceService;
import com.hr.employee_management.enums.AttendanceStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceMapper attendanceMapper;

    @Override
    @Transactional
    public AttendanceResponse recordAttendance(AttendanceRequest request){
        Employee employee = employeeRepository.findById(request.getEmployeeId()).orElseThrow(() ->
                new ResourceNotFoundException("Employee", "id", request.getEmployeeId()));
        if(attendanceRepository.existsByEmployeeIdAndAttendanceDate(request.getEmployeeId(), request.getAttendanceDate())) {
            throw new DuplicateResourceException("Attendance record already exists for employee " +
                    employee.getEmployeeCode() + " on " + request.getAttendanceDate());
        }
        validateAttendanceTimesAndOvertime(request);

        Attendance attendance = attendanceMapper.toEntity(request, employee);
        Attendance saved = attendanceRepository.save(attendance);
        log.info("Attendance ID {} recorded for employee ID {} on {}", saved.getId(), employee.getId(), saved.getAttendanceDate());
        return attendanceMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AttendanceResponse> getAllAttendance(LocalDate date, LocalDate from, LocalDate to, Pageable pageable){
        if (date != null){
            return attendanceRepository.findByAttendanceDate(date, pageable).map(attendanceMapper::toResponse);
        } else if(from != null && to != null){
            if (to.isBefore(from)){
                throw new BusinessRuleException("'to' date must not precede 'from' date");
            }
            return attendanceRepository.findByAttendanceDateBetween(from, to, pageable).map(attendanceMapper::toResponse);
        } else {
            return attendanceRepository.findAll(pageable).map(attendanceMapper::toResponse);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceResponse getAttendanceById(Long id){
        Attendance attendance = attendanceRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Attendance", "id", id));
        return attendanceMapper.toResponse(attendance);
    }

    @Override
    @Transactional
    public AttendanceResponse updateAttendance(Long id, AttendanceRequest request){
        Attendance attendance = attendanceRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Attendance", "id", id));

        Employee employee = employeeRepository.findById(request.getEmployeeId()).orElseThrow(() ->
                new ResourceNotFoundException("Employee", "id", request.getEmployeeId()));
        if (attendanceRepository.existsByEmployeeIdAndAttendanceDateAndIdNot(request.getEmployeeId(), request.getAttendanceDate(), id)){
            throw new DuplicateResourceException("Another attendance record already exists for employee " +
                    employee.getEmployeeCode() + " on " + request.getAttendanceDate());
        }
        validateAttendanceTimesAndOvertime(request);

        attendance.setEmployee(employee);
        attendance.setAttendanceDate(request.getAttendanceDate());
        attendance.setCheckIn(request.getCheckIn());
        attendance.setCheckOut(request.getCheckOut());
        attendance.setStatus(request.getStatus());
        attendance.setOvertimeHours(request.getOvertimeHours() != null ? request.getOvertimeHours() : BigDecimal.ZERO);
        attendance.setRemarks(request.getRemarks());

        Attendance updated = attendanceRepository.save(attendance);
        log.info("Attendance ID {} updated", id);
        return attendanceMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AttendanceResponse> getAttendanceByEmployee(Long employeeId, Pageable pageable){
        if (!employeeRepository.existsById(employeeId)){
            throw new ResourceNotFoundException("Employee", "id", employeeId);
        }
        return attendanceRepository.findByEmployeeId(employeeId, pageable).map(attendanceMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceSummaryResponse getAttendanceSummary(int month, int year){
        if (month < 1 || month > 12){
            throw new BusinessRuleException("Month must be between 1 and 12");
        }
        if (year < 2000){
            throw new BusinessRuleException("Year must be 2000 or later");
        }
        List<Attendance> records = attendanceRepository.findByMonthAndYear(month, year);
        long present = 0;
        long absent = 0;
        long late = 0;
        long haftDay = 0;
        long onLeave = 0;
        BigDecimal totalOvertime = BigDecimal.ZERO;

        for (Attendance a : records) {
            if (a.getStatus() == AttendanceStatus.PRESENT) present++;
            else if (a.getStatus() == AttendanceStatus.ABSENT) absent++;
            else if (a.getStatus() == AttendanceStatus.LATE) late++;
            else if (a.getStatus() == AttendanceStatus.HALF_DAY) haftDay++;
            else if (a.getStatus() == AttendanceStatus.ON_LEAVE) onLeave++;

            if (a.getOvertimeHours() != null) {
                totalOvertime = totalOvertime.add(a.getOvertimeHours());
            }
        }
        return AttendanceSummaryResponse.builder()
                .month(month)
                .year(year)
                .totalRecords(records.size())
                .presentCount(present)
                .absentCount(absent)
                .lateCount(late)
                .halfDayCount(haftDay)
                .onLeaveCount(onLeave)
                .totalOvertimeHours(totalOvertime)
                .build();
    }

    private void validateAttendanceTimesAndOvertime(AttendanceRequest request){
        if (request.getCheckIn() != null && request.getCheckOut() != null){
            if (request.getCheckOut().isBefore(request.getCheckIn())){
                throw new BusinessRuleException("Check-out time cannot be earlier than check-in time");
            }
        }
        if (request.getOvertimeHours() != null && request.getOvertimeHours().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException("Overtime hours cannot be negative");
        }
    }
}
