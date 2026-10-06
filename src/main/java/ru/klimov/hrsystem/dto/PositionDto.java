package ru.klimov.hrsystem.dto;

import ru.klimov.hrsystem.model.PositionType;

import java.math.BigDecimal;

public record PositionDto(
        Long id,
        PositionType type,
        String title,
        BigDecimal baseSalary,
        String techStack,
        Integer grade,
        Integer teamSize,
        BigDecimal salesPercent
) {
}
