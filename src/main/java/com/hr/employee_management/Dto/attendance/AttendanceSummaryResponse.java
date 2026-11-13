package com.hr.employee_management.Dto.attendance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttendanceSummaryResponse {
    private int month;
    private int year;
    private long totalRecords;
    private long presentCount;
    private long absentCount;
    private long lateCount;
    private long halfDayCount;
    private long onLeaveCount;
    private BigDecimal totalOvertimeHours;
}
