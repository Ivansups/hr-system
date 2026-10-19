package ru.klimov.hrsystem.repository.jdbc;

import org.junit.jupiter.api.Test;
import ru.klimov.hrsystem.exception.ValidationException;
import ru.klimov.hrsystem.model.Department;
import ru.klimov.hrsystem.model.DeveloperPosition;
import ru.klimov.hrsystem.model.Employee;
import ru.klimov.hrsystem.model.Position;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;

class JdbcEmployeeRepositoryTest extends JdbcTestSupport {

    private final JdbcDepartmentRepository departments = new JdbcDepartmentRepository(dataSource);
    private final JdbcPositionRepository positions = new JdbcPositionRepository(dataSource);
    private final JdbcEmployeeRepository repository = new JdbcEmployeeRepository(dataSource, departments, positions);

    @Test
    void saveThrowsValidationWhenFullNameIsTooLong() {
        Department department = departments.save(new Department("Engineering"));
        Position position = positions.save(new DeveloperPosition("Backend Developer", new BigDecimal("2000.00"), "Java", 3));
        Employee employee = new Employee("x".repeat(300), LocalDate.of(2024, 1, 1), department, position);

        assertThrows(ValidationException.class, () -> repository.save(employee));
    }

    @Test
    void saveThrowsValidationWhenHireDateIsOutOfDatabaseRange() {
        Department department = departments.save(new Department("Engineering"));
        Position position = positions.save(new DeveloperPosition("Backend Developer", new BigDecimal("2000.00"), "Java", 3));
        Employee employee = new Employee("Ivan Petrov", LocalDate.of(6_000_000, 1, 1), department, position);

        assertThrows(ValidationException.class, () -> repository.save(employee));
    }
}
