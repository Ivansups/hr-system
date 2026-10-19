package ru.klimov.hrsystem.repository.jdbc;

import org.junit.jupiter.api.Test;
import ru.klimov.hrsystem.exception.ConflictException;
import ru.klimov.hrsystem.exception.ValidationException;
import ru.klimov.hrsystem.model.DeveloperPosition;
import ru.klimov.hrsystem.model.Employee;
import ru.klimov.hrsystem.model.Department;
import ru.klimov.hrsystem.model.Position;
import ru.klimov.hrsystem.repository.DepartmentRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class JdbcDepartmentRepositoryTest extends JdbcTestSupport {

    private final DepartmentRepository repository = new JdbcDepartmentRepository(dataSource);

    @Test
    void savingAssignsAnId() {
        Department saved = repository.save(new Department("Engineering"));
        assertNotNull(saved.getId());
    }

    @Test
    void findByIdReturnsSavedDepartment() {
        Department saved = repository.save(new Department("Engineering"));
        Optional<Department> found = repository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Engineering", found.get().getName());
    }

    @Test
    void updateChangesTheStoredName() {
        Department saved = repository.save(new Department("Engineering"));
        saved.setName("Platform Engineering");
        repository.save(saved);

        Optional<Department> found = repository.findById(saved.getId());
        assertEquals("Platform Engineering", found.get().getName());
    }

    @Test
    void deleteByIdRemovesTheRow() {
        Department saved = repository.save(new Department("Engineering"));
        repository.deleteById(saved.getId());
        assertTrue(repository.findById(saved.getId()).isEmpty());
    }

    @Test
    void deleteByIdThrowsConflictWhenEmployeesReferenceTheDepartment() {
        JdbcPositionRepository positions = new JdbcPositionRepository(dataSource);
        Department department = repository.save(new Department("Engineering"));
        Position position = positions.save(new DeveloperPosition("Backend Developer", new BigDecimal("2000.00"), "Java", 3));
        new JdbcEmployeeRepository(dataSource, repository, positions)
                .save(new Employee("Ivan Petrov", LocalDate.of(2024, 1, 1), department, position));

        assertThrows(ConflictException.class, () -> repository.deleteById(department.getId()));
    }

    @Test
    void saveThrowsValidationWhenNameIsTooLong() {
        assertThrows(ValidationException.class, () -> repository.save(new Department("x".repeat(300))));
    }

    @Test
    void saveThrowsValidationWhenNameContainsNulCharacter() {
        assertThrows(ValidationException.class, () -> repository.save(new Department("a\u0000b")));
    }
}
