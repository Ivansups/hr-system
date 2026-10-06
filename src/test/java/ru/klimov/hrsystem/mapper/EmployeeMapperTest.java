package ru.klimov.hrsystem.mapper;

import org.junit.jupiter.api.Test;
import ru.klimov.hrsystem.dto.EmployeeDto;
import ru.klimov.hrsystem.model.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeMapperTest {

    @Test
    void toDtoDenormalizesDepartmentAndPosition() {
        Department department = new Department("Engineering");
        department.setId(1L);
        ManagerPosition position = new ManagerPosition("Team Lead", new BigDecimal("3000"), 5);
        position.setId(2L);
        Employee employee = new Employee("Ivan Petrov", LocalDate.of(2024, 1, 1), department, position);
        employee.setId(3L);

        EmployeeDto dto = EmployeeMapper.toDto(employee);

        assertEquals(3L, dto.id());
        assertEquals("Engineering", dto.departmentName());
        assertEquals(PositionType.MANAGER, dto.positionType());
        assertEquals(5, dto.teamSize());
    }
}
