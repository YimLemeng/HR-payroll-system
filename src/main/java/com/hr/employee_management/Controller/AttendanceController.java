package com.hr.employee_management.Controller;

import com.hr.employee_management.Dto.attendance.AttendanceRequest;
import com.hr.employee_management.Dto.attendance.AttendanceResponse;
import com.hr.employee_management.Dto.attendance.AttendanceSummaryResponse;
import com.hr.employee_management.Service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'HR')")
@Tag(name = "Attendance Management", description = "Endpoints for clock-in/out logging, manual corrections, date range filtering, and monthly summaries")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping
    @Operation(summary = "Record attendance", description = "Record daily attendance for an employee. Prevents duplicate records for the same employee and date.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Attendance recorded successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed or check-out earlier than check-in"),
            @ApiResponse(responseCode = "409", description = "Duplicate attendance for employee and date")
    })
    public ResponseEntity<AttendanceResponse> recordAttendance(@Valid @RequestBody AttendanceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(attendanceService.recordAttendance(request));
    }

    @GetMapping
    @Operation(summary = "Get attendance records with filtering", description = "Filter by specific date or date range (from and to), with pagination.")
    public ResponseEntity<Page<AttendanceResponse>> getAllAttendance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @PageableDefault(size = 10, sort = "attendanceDate") Pageable pageable) {
        return ResponseEntity.ok(attendanceService.getAllAttendance(date, from, to, pageable));
    }

    @GetMapping("/summary")
    @Operation(summary = "Get monthly attendance summary", description = "Returns aggregated counts of present, absent, late, half-day, and total overtime hours.")
    public ResponseEntity<AttendanceSummaryResponse> getAttendanceSummary(
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(attendanceService.getAttendanceSummary(month, year));
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Get attendance by employee ID", description = "Paginated attendance history for a single employee.")
    public ResponseEntity<Page<AttendanceResponse>> getAttendanceByEmployee(
            @PathVariable Long employeeId,
            @PageableDefault(size = 10, sort = "attendanceDate") Pageable pageable) {
        return ResponseEntity.ok(attendanceService.getAttendanceByEmployee(employeeId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get attendance record by ID", description = "Retrieve a single attendance record by ID.")
    public ResponseEntity<AttendanceResponse> getAttendanceById(@PathVariable Long id) {
        return ResponseEntity.ok(attendanceService.getAttendanceById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update / correct attendance record", description = "Correct an attendance record (e.g. check-in/out times, overtime, remarks).")
    public ResponseEntity<AttendanceResponse> updateAttendance(
            @PathVariable Long id,
            @Valid @RequestBody AttendanceRequest request) {
        return ResponseEntity.ok(attendanceService.updateAttendance(id, request));
    }
}
