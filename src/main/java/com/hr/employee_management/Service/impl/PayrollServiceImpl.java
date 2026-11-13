package com.hr.employee_management.Service.impl;

import com.hr.employee_management.Dto.payroll.PayrollResponse;
import com.hr.employee_management.Dto.payroll.PayrollSummaryResponse;
import com.hr.employee_management.Entity.Attendance;
import com.hr.employee_management.Entity.Employee;
import com.hr.employee_management.Entity.Payroll;
import com.hr.employee_management.Exception.BusinessRuleException;
import com.hr.employee_management.Exception.ResourceNotFoundException;
import com.hr.employee_management.Mapper.PayrollMapper;
import com.hr.employee_management.Repository.AttendanceRepository;
import com.hr.employee_management.Repository.EmployeeRepository;
import com.hr.employee_management.Repository.PayrollRepository;
import com.hr.employee_management.Service.PayrollService;
import com.hr.employee_management.enums.EmploymentStatus;
import com.hr.employee_management.enums.PayrollStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayrollServiceImpl implements PayrollService {
    private final PayrollRepository payrollRepository;
    private final EmployeeRepository employeeRepository;
    private final PayrollMapper payrollMapper;
    private final AttendanceRepository attendanceRepository;

    private static final BigDecimal STANDARD_MONTHLY_HOURS = BigDecimal.valueOf(160);
    private static final BigDecimal OVERTIME_MULTIPLIER = BigDecimal.valueOf(1.5);
    private static final BigDecimal ALLOWANCE_RATE = BigDecimal.valueOf(0.10);
    private static final BigDecimal DEDUCTION_RATE = BigDecimal.valueOf(0.05);
    private static final BigDecimal TAX_RATE = BigDecimal.valueOf(0.10);

    @Override
    @Transactional
    public List<PayrollResponse> calculateMonthlyPayroll(int month, int year, Long departmentId){
        if (month < 1 || month > 12) {
            throw new BusinessRuleException("Month must be between 1 and 12");
        }
        if (year < 2000){
            throw new BusinessRuleException("Year must be 2000 or later");
        }
        List<Employee> eligibleEmployees;
        if (departmentId != null){
            eligibleEmployees = employeeRepository.findByDepartmentIdAndEmploymentStatus(departmentId, EmploymentStatus.ACTIVE);
        } else {
            eligibleEmployees = employeeRepository.findByEmploymentStatus(EmploymentStatus.ACTIVE);
        }

        if (eligibleEmployees.isEmpty()){
            log.info("No active eligible employees found for payroll calculation (month: {}, year: {}, departmentId: {})",
                    month, year, departmentId);
            return Collections.emptyList();
        }
        List<Attendance> monthlyAttendances = attendanceRepository.findByMonthAndYear(month, year);
        Map<Long, BigDecimal> employeeOvertimeHours = new HashMap<>();
        for (Attendance att : monthlyAttendances){
            if (att.getOvertimeHours() != null && att.getOvertimeHours().compareTo(BigDecimal.ZERO) > 0){
                Long empId = att.getEmployee().getId();
                employeeOvertimeHours.merge(empId, att.getOvertimeHours(), BigDecimal::add);
            }
        }

        List<Payroll> payrollsToSave = new ArrayList<>();
        for (Employee emp : eligibleEmployees){
            Optional<Payroll> existingOpt = payrollRepository.findByEmployeeIdAndPayrollYearAndPayrollMonth(emp.getId(), year, month);
            if (existingOpt.isPresent()){
                Payroll existing = existingOpt.get();
                if (existing.getStatus() == PayrollStatus.PAID){
                    log.warn("Skipping payroll calculation for employee ID {} because it is already PAID", emp.getId());
                    continue;
                }
                calculateAndPopulatePayroll(existing, emp, month, year, employeeOvertimeHours.getOrDefault(emp.getId(), BigDecimal.ZERO));
                payrollsToSave.add(existing);
            } else {
                Payroll payroll = new Payroll();
                calculateAndPopulatePayroll(payroll, emp, month, year, employeeOvertimeHours.getOrDefault(emp.getId(), BigDecimal.ZERO));
                payrollsToSave.add(payroll);
            }
        }

        List<Payroll> savedPayrolls = payrollRepository.saveAll(payrollsToSave);
        log.info("Processed {} payroll records for {}/{}", savedPayrolls.size(), month, year);
        return savedPayrolls.stream().map(payrollMapper::toResponse).collect(Collectors.toList());
    }

    private void calculateAndPopulatePayroll(Payroll payroll, Employee emp, int month, int year, BigDecimal overtimeHours){
        BigDecimal basicSalary = emp.getBasicSalary().setScale(2, RoundingMode.HALF_UP);
        BigDecimal allowances = basicSalary.multiply(ALLOWANCE_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal hourlyRate = basicSalary.divide(STANDARD_MONTHLY_HOURS, 4, RoundingMode.HALF_UP);
        BigDecimal overtimePay = overtimeHours.multiply(hourlyRate).multiply(OVERTIME_MULTIPLIER).setScale(2, RoundingMode.HALF_UP);
        BigDecimal bonus = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal deductions = basicSalary.multiply(DEDUCTION_RATE).setScale(2, RoundingMode.HALF_UP);

        BigDecimal grossSalary = basicSalary.add(allowances).add(overtimePay).add(bonus).setScale(2, RoundingMode.HALF_UP);
        BigDecimal tax = grossSalary.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal netSalary = grossSalary.subtract(deductions).subtract(tax).setScale(2, RoundingMode.HALF_UP);

        payroll.setEmployee(emp);
        payroll.setPayrollMonth(month);
        payroll.setPayrollYear(year);
        payroll.setBasicSalary(basicSalary);
        payroll.setAllowances(allowances);
        payroll.setOvertimePay(overtimePay);
        payroll.setBonus(bonus);
        payroll.setDeductions(deductions);
        payroll.setTax(tax);
        payroll.setGrossSalary(grossSalary);
        payroll.setNetSalary(netSalary);
        payroll.setStatus(PayrollStatus.CALCULATED);
        payroll.setCreatedAt(Instant.now());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PayrollResponse> getPayrolls(Integer month, Integer year, PayrollStatus status, Pageable pageable){
        if (month != null && year != null && status != null){
            return payrollRepository.findByPayrollYearAndPayrollMonthAndStatus(year, month, status, pageable)
                    .map(payrollMapper::toResponse);
        } else if (month != null && year != null){
            return payrollRepository.findByPayrollYearAndPayrollMonth(year, month, pageable).map(payrollMapper::toResponse);
        }
        return payrollRepository.findAll(pageable).map(payrollMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PayrollResponse getPayrollById(Long id){
        Payroll payroll = payrollRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Payroll", "id", id));
        return payrollMapper.toResponse(payroll);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PayrollResponse> getPayrollsByEmployee(Long employeeId, Pageable pageable){
        if (!employeeRepository.existsById(employeeId)){
            throw new ResourceNotFoundException("Employee", "id", employeeId);
        }
        return payrollRepository.findByEmployeeId(employeeId, pageable).map(payrollMapper::toResponse);
    }

    @Override
    @Transactional
    public PayrollResponse approvePayroll(Long id){
        Payroll payroll = payrollRepository.findById(id).orElseThrow(()
                -> new ResourceNotFoundException("Payroll", "id", id));
        if (payroll.getStatus() == PayrollStatus.PAID){
            throw new BusinessRuleException("Cannot alter an already PAID payroll record");
        }
        if (payroll.getStatus() == PayrollStatus.CANCELLED){
            throw new BusinessRuleException("Cannot approve a CANCELLED payroll record");
        }

        payroll.setStatus(PayrollStatus.APPROVED);
        Payroll updated = payrollRepository.save(payroll);
        log.info("Payroll ID {} approved", id);
        return payrollMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public PayrollResponse markPayrollPaid(Long id){
        Payroll payroll = payrollRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Payroll", "id", id));
        if (payroll.getStatus() != PayrollStatus.APPROVED){
            throw new BusinessRuleException("Only APPROVED payroll records can be marked as PAID. Current status: " + payroll.getStatus());
        }

        payroll.setStatus(PayrollStatus.PAID);
        payroll.setPaidAt(Instant.now());
        Payroll updated = payrollRepository.save(payroll);
        log.info("Payroll ID {} marked as PAID", id);
        return payrollMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public PayrollSummaryResponse getPayrollSummary(int month, int year){
        if (month < 1 || month > 12){
            throw new BusinessRuleException("Month must be between 1 and 12");
        }
        if (year < 2000){
            throw new BusinessRuleException("Year must be 2000 or later");
        }
        List<Payroll> records = payrollRepository.findByPayrollYearAndPayrollMonth(year, month);

        BigDecimal totalGross = BigDecimal.ZERO;
        BigDecimal totalNet = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;
        BigDecimal totalDeductions = BigDecimal.ZERO;
        BigDecimal totalOvertime = BigDecimal.ZERO;
        BigDecimal totalAllowances = BigDecimal.ZERO;
        BigDecimal totalBonus = BigDecimal.ZERO;
        Map<String, Long> statusCounts = new HashMap<>();

        for (Payroll p : records) {
            totalGross = totalGross.add(p.getGrossSalary());
            totalNet = totalNet.add(p.getNetSalary());
            totalTax = totalTax.add(p.getTax());
            totalDeductions = totalDeductions.add(p.getDeductions());
            totalOvertime = totalOvertime.add(p.getOvertimePay());
            totalAllowances = totalAllowances.add(p.getAllowances());
            totalBonus = totalBonus.add(p.getBonus());
            String statusStr = p.getStatus().name();
            statusCounts.put(statusStr, statusCounts.getOrDefault(statusStr, 0L) + 1);
        }

        return PayrollSummaryResponse.builder()
                .month(month)
                .year(year)
                .totalRecords(records.size())
                .totalGrossSalary(totalGross.setScale(2, RoundingMode.HALF_UP))
                .totalNetSalary(totalNet.setScale(2, RoundingMode.HALF_UP))
                .totalTax(totalTax.setScale(2, RoundingMode.HALF_UP))
                .totalDeductions(totalDeductions.setScale(2, RoundingMode.HALF_UP))
                .totalOvertimePay(totalOvertime.setScale(2, RoundingMode.HALF_UP))
                .totalAllowances(totalAllowances.setScale(2, RoundingMode.HALF_UP))
                .totalBonus(totalBonus.setScale(2, RoundingMode.HALF_UP))
                .statusCounts(statusCounts)
                .build();
    }
}
