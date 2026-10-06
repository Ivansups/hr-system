package ru.klimov.hrsystem.dto;

import ru.klimov.hrsystem.model.PositionType;

import java.math.BigDecimal;
import java.time.LocalDate;

// On create/update, only fullName/hireDate/departmentId/positionId are read;
// the remaining fields are denormalized data filled in on responses.
public record EmployeeDto(
        Long id,
        String fullName,
        LocalDate hireDate,
        Long departmentId,
        String departmentName,
        Long positionId,
        PositionType positionType,
        String positionTitle,
        BigDecimal baseSalary,
        String techStack,
        Integer grade,
        Integer teamSize,
        BigDecimal salesPercent
) {
}
