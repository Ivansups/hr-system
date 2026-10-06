package ru.klimov.hrsystem.mapper;

import ru.klimov.hrsystem.dto.DepartmentDto;
import ru.klimov.hrsystem.model.Department;

public final class DepartmentMapper {
    private DepartmentMapper() {
    }

    public static DepartmentDto toDto(Department department) {
        return new DepartmentDto(department.getId(), department.getName());
    }

    public static Department toEntity(DepartmentDto dto) {
        Department department = new Department(dto.name());
        department.setId(dto.id());
        return department;
    }
}
