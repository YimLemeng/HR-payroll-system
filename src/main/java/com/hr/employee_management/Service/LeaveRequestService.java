package com.hr.employee_management.Service;

import com.hr.employee_management.Dto.leave.LeaveRequestDto;
import com.hr.employee_management.Dto.leave.LeaveResponse;
import com.hr.employee_management.Dto.leave.LeaveReviewRequest;
import com.hr.employee_management.enums.LeaveStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LeaveRequestService {
    LeaveResponse submitLeaveRequest(LeaveRequestDto request);
    Page<LeaveResponse> getAllLeaves(LeaveStatus status, Pageable pageable);
    LeaveResponse getLeaveById(Long id);
    Page<LeaveResponse> getLeavesByEmployee(Long employeeId, Pageable pageable);
    LeaveResponse approveLeave(Long id, LeaveReviewRequest reviewRequest);
    LeaveResponse rejectLeave(Long id, LeaveReviewRequest reviewRequest);
    LeaveResponse cancelLeave(Long id);
}
