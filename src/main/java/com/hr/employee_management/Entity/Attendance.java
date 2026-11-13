package com.hr.employee_management.Entity;

import com.hr.employee_management.enums.AttendanceStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
        name = "attendances",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_employee_attendance_date", columnNames = {"employee_id", "attendance_date"})
        })
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Attendance extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY,  optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Column(name = "check_in")
    private Instant checkIn;

    @Column(name = "check_out")
    private Instant checkOut;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttendanceStatus status;

    @Column(name = "overtime_hours", nullable = false, precision = 4, scale = 2)
    private BigDecimal overtimeHours = BigDecimal.ZERO;

    @Column(length = 255)
    private String remarks;

    @PrePersist
    protected void onPrePersist() {
        if (this.overtimeHours == null) {
            this.overtimeHours = BigDecimal.ZERO;
        }
    }
}
