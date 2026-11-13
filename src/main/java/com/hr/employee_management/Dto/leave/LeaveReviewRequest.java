package com.hr.employee_management.Dto.leave;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LeaveReviewRequest {

    @Size(max = 500, message = "Comment must not exceed 500 characters")
    private String comment;
}
