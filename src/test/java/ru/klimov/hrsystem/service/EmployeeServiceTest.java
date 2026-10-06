package ru.klimov.hrsystem.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.klimov.hrsystem.dto.EmployeeDto;
import ru.klimov.hrsystem.exception.EntityNotFoundException;
import ru.klimov.hrsystem.exception.ValidationException;
import ru.klimov.hrsystem.model.*;
import ru.klimov.hrsystem.repository.DepartmentRepository;
import ru.klimov.hrsystem.repository.EmployeeRepository;
import ru.klimov.hrsystem.repository.PositionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock private EmployeeRepository employeeRepository;
    @Mock private DepartmentRepository departmentRepository;
    @Mock private PositionRepository positionRepository;

    private EmployeeService service() {
        return new EmployeeService(employeeRepository, departmentRepository, positionRepository);
    }

    @Test
    void createFailsWhenDepartmentDoesNotExist() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.empty());

        EmployeeDto input = new EmployeeDto(null, "Ivan Petrov", LocalDate.of(2024, 1, 1),
                1L, null, 2L, null, null, null, null, null, null, null);

        assertThrows(EntityNotFoundException.class, () -> service().create(input));
    }

    @Test
    void createFailsWhenHireDateIsInTheFuture() {
        // No repository stubs needed: validate() rejects the future hireDate
        // before EmployeeService ever resolves the department or position.
        EmployeeDto input = new EmployeeDto(null, "Ivan Petrov", LocalDate.now().plusDays(1),
                1L, null, 2L, null, null, null, null, null, null, null);

        assertThrows(ValidationException.class, () -> service().create(input));
    }

    @Test
    void createSavesEmployeeWhenReferencesAreValid() {
        Department department = new Department("Engineering");
        department.setId(1L);
        DeveloperPosition position = new DeveloperPosition("Backend Developer", new BigDecimal("2000"), "Java", 3);
        position.setId(2L);

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(positionRepository.findById(2L)).thenReturn(Optional.of(position));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
            Employee employee = invocation.getArgument(0);
            employee.setId(10L);
            return employee;
        });

        EmployeeDto input = new EmployeeDto(null, "Ivan Petrov", LocalDate.of(2024, 1, 1),
                1L, null, 2L, null, null, null, null, null, null, null);

        EmployeeDto created = service().create(input);

        assertEquals(10L, created.id());
        assertEquals("Engineering", created.departmentName());
        assertEquals(PositionType.DEVELOPER, created.positionType());
    }
}
