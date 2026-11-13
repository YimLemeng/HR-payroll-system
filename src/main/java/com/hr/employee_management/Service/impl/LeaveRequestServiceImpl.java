package com.hr.employee_management.Service.impl;

import com.hr.employee_management.Dto.leave.LeaveRequestDto;
import com.hr.employee_management.Dto.leave.LeaveResponse;
import com.hr.employee_management.Dto.leave.LeaveReviewRequest;
import com.hr.employee_management.Entity.Employee;
import com.hr.employee_management.Entity.LeaveRequest;
import com.hr.employee_management.Entity.User;
import com.hr.employee_management.Exception.BusinessRuleException;
import com.hr.employee_management.Exception.ResourceNotFoundException;
import com.hr.employee_management.Mapper.LeaveRequestMapper;
import com.hr.employee_management.Repository.EmployeeRepository;
import com.hr.employee_management.Repository.LeaveRequestRepository;
import com.hr.employee_management.Repository.UserRepository;
import com.hr.employee_management.Service.LeaveRequestService;
import com.hr.employee_management.enums.LeaveStatus;
import com.hr.employee_management.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveRequestServiceImpl implements LeaveRequestService {
    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveRequestMapper leaveRequestMapper;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public LeaveResponse submitLeaveRequest(LeaveRequestDto request) {
        Employee emp = employeeRepository.findById(request.getEmployeeId()).orElseThrow(() ->
                new ResourceNotFoundException("Employee", "id", request.getEmployeeId()));
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BusinessRuleException("Leave end date cannot precede start date");
        }
        if(leaveRequestRepository.hasOverlappingApprovedLeave(emp.getId(), request.getStartDate(), request.getEndDate())){
            throw new BusinessRuleException("Employee already has an approved leave request overlapping with this date period");
        }

        LeaveRequest leave = leaveRequestMapper.toEntity(request, emp);
        LeaveRequest saved = leaveRequestRepository.save(leave);
        log.info("Leave request ID {} submitted for employee ID {}", saved.getId(), emp.getId());
        return leaveRequestMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeaveResponse> getAllLeaves(LeaveStatus status, Pageable pageable){
        if (status != null){
            return leaveRequestRepository.findByStatus(status, pageable).map(leaveRequestMapper::toResponse);
        } else {
            return leaveRequestRepository.findAll(pageable).map(leaveRequestMapper::toResponse);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public LeaveResponse getLeaveById(Long id) {
        LeaveRequest leave = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", "id", id));
        return leaveRequestMapper.toResponse(leave);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeaveResponse> getLeavesByEmployee(Long employeeId, Pageable pageable){
        if (!employeeRepository.existsById(employeeId)){
            throw new ResourceNotFoundException("Employee", "id", employeeId);
        }
        return leaveRequestRepository.findByEmployeeId(employeeId, pageable).map(leaveRequestMapper::toResponse);
    }

    @Override
    @Transactional
    public LeaveResponse approveLeave(Long id, LeaveReviewRequest reviewRequest){
        LeaveRequest leave = leaveRequestRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("LeaveRequest", "id", id));
        if (leave.getStatus() != LeaveStatus.PENDING){
            throw new BusinessRuleException("Only PENDING leave requests can be approved. Current status: " + leave.getStatus());
        }
        if (leaveRequestRepository.hasOverlappingApprovedLeaveExcluding(leave.getEmployee().getId(), leave.getStartDate(), leave.getEndDate(), leave.getId())){
            throw new BusinessRuleException("Cannot approve: Employee already has an overlapping approved leave");
        }

        User reviewer = getCurrentUser();
        leave.setStatus(LeaveStatus.APPROVED);
        leave.setReviewedBy(reviewer);
        leave.setReviewedAt(Instant.now());
        if (reviewRequest != null && reviewRequest.getComment() != null){
            leave.setReviewComment(reviewRequest.getComment().trim());
        }
        LeaveRequest updated = leaveRequestRepository.save(leave);
        log.info("Leave request ID {} approved by user '{}'", id, reviewer != null ? reviewer.getUsername() : "SYSTEM");
        return leaveRequestMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public LeaveResponse rejectLeave(Long id, LeaveReviewRequest reviewRequest){
        LeaveRequest leave = leaveRequestRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("LeaveRequest", "id", id));
        if (leave.getStatus() != LeaveStatus.PENDING){
            throw new BusinessRuleException("Only PENDING leave requests can be rejected. Current status: " + leave.getStatus());
        }

        User reviewer = getCurrentUser();
        leave.setStatus(LeaveStatus.REJECTED);
        leave.setReviewedBy(reviewer);
        leave.setReviewedAt(Instant.now());
        if (reviewRequest != null && reviewRequest.getComment() != null){
            leave.setReviewComment(reviewRequest.getComment().trim());
        }
        LeaveRequest updated = leaveRequestRepository.save(leave);
        log.info("Leave request ID {} rejected by user '{}'", id, reviewer != null ? reviewer.getUsername() : "SYSTEM");
        return leaveRequestMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public LeaveResponse cancelLeave(Long id){
        LeaveRequest leave = leaveRequestRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("LeaveRequest", "id", id));
        if (leave.getStatus() == LeaveStatus.REJECTED){
            throw new BusinessRuleException("Cannot cancel a REJECTED leave request");
        }
        if (leave.getStatus() == LeaveStatus.CANCELLED){
            throw new BusinessRuleException("Leave request is already CANCELLED");
        }

        leave.setStatus(LeaveStatus.CANCELLED);
        LeaveRequest updated = leaveRequestRepository.save(leave);
        log.info("Leave request ID {} cancelled", id);
        return leaveRequestMapper.toResponse(updated);
    }

    private User getCurrentUser(){
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId != null){
            return userRepository.findById(userId).orElse(null);
        }
        return null;
    }
}
