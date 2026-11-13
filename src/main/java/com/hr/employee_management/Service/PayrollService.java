package com.hr.employee_management.Service;

import com.hr.employee_management.Dto.payroll.PayrollResponse;
import com.hr.employee_management.Dto.payroll.PayrollSummaryResponse;
import com.hr.employee_management.enums.PayrollStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PayrollService {
    List<PayrollResponse> calculateMonthlyPayroll(int month, int year, Long departmentId);
    Page<PayrollResponse> getPayrolls(Integer month, Integer year, PayrollStatus status, Pageable pageable);
    PayrollResponse getPayrollById(Long id);
    Page<PayrollResponse> getPayrollsByEmployee(Long employeeId, Pageable pageable);
    PayrollResponse approvePayroll(Long id);
    PayrollResponse markPayrollPaid(Long id);
    PayrollSummaryResponse getPayrollSummary(int month, int year);
}
