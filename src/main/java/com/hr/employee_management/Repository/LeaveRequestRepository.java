package com.hr.employee_management.Repository;

import com.hr.employee_management.Entity.LeaveRequest;
import com.hr.employee_management.enums.LeaveStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    Page<LeaveRequest> findByEmployeeId(Long employeeId, Pageable pageable);
    Page<LeaveRequest> findByStatus(LeaveStatus status, Pageable pageable);

    @Query("SELECT count(l) > 0 FROM LeaveRequest l " +
            "WHERE l.employee.id = :empId " +
            "AND l.status = com.hr.employee_management.enums.LeaveStatus.APPROVED " +
            "AND l.startDate <= :endDate " +
            "AND l.endDate >= :startDate")
    boolean hasOverlappingApprovedLeave(
            @Param("empId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT count(l) > 0 FROM LeaveRequest l " +
            "WHERE l.employee.id = :empId " +
            "AND l.status = com.hr.employee_management.enums.LeaveStatus.APPROVED " +
            "AND l.id != :excludeId " +
            "AND l.startDate <= :endDate " +
            "AND l.endDate >= :startDate")
    boolean hasOverlappingApprovedLeaveExcluding(
            @Param("empId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("excludeId") Long excludeId);
}
