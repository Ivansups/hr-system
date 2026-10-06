package ru.klimov.hrsystem.mapper;

import ru.klimov.hrsystem.dto.EmployeeDto;
import ru.klimov.hrsystem.dto.PositionDto;
import ru.klimov.hrsystem.model.Employee;

public final class EmployeeMapper {
    private EmployeeMapper() {
    }

    public static EmployeeDto toDto(Employee employee) {
        PositionDto positionDto = PositionMapper.toDto(employee.getPosition());
        return new EmployeeDto(
                employee.getId(),
                employee.getFullName(),
                employee.getHireDate(),
                employee.getDepartment().getId(),
                employee.getDepartment().getName(),
                positionDto.id(),
                positionDto.type(),
                positionDto.title(),
                positionDto.baseSalary(),
                positionDto.techStack(),
                positionDto.grade(),
                positionDto.teamSize(),
                positionDto.salesPercent()
        );
    }
}
