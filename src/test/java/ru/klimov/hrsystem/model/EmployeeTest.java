package ru.klimov.hrsystem.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EmployeeTest {

    @Test
    void exposesDepartmentAndConcretePositionType() {
        Department department = new Department("Engineering");
        DeveloperPosition position = new DeveloperPosition("Backend Developer", new BigDecimal("2000.00"), "Java", 3);
        Employee employee = new Employee("Ivan Petrov", LocalDate.of(2024, 1, 15), department, position);

        assertEquals("Ivan Petrov", employee.getFullName());
        assertEquals(department, employee.getDepartment());
        assertEquals(PositionType.DEVELOPER, employee.getPosition().getType());
        assertEquals("Java", ((DeveloperPosition) employee.getPosition()).getTechStack());
    }
}
