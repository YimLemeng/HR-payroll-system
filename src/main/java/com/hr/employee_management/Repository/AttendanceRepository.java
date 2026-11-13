package com.hr.employee_management.Repository;

import com.hr.employee_management.Entity.Attendance;
import com.hr.employee_management.enums.AttendanceStatus;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByEmployeeIdAndAttendanceDate(Long employeeId, LocalDate attendanceDate);
    boolean existsByEmployeeIdAndAttendanceDate(Long employeeId, LocalDate attendanceDate);
    boolean existsByEmployeeIdAndAttendanceDateAndIdNot(Long employeeId, LocalDate attendanceDate, Long id);
    Page<Attendance> findByAttendanceDate(LocalDate attendanceDate, Pageable pageable);
    Page<Attendance> findByAttendanceDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
    Page<Attendance> findByEmployeeId(Long employeeId, Pageable pageable);
    Page<Attendance> findByEmployeeIdAndAttendanceDateBetween(Long employeeId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    @Query("SELECT a from Attendance a where " +
            "extract(month from a.attendanceDate) = :month AND " +
            "extract(year from a.attendanceDate) = :year")
    List<Attendance> findByMonthAndYear(@Param("month") int month, @Param("year") int year);
    long countByAttendanceDateBetweenAndStatus(LocalDate startDate, LocalDate endDate, AttendanceStatus status);

    List<Attendance> id(Long id);
}
