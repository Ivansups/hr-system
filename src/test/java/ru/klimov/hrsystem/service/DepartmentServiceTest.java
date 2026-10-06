package ru.klimov.hrsystem.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.klimov.hrsystem.dto.DepartmentDto;
import ru.klimov.hrsystem.exception.ValidationException;
import ru.klimov.hrsystem.model.Department;
import ru.klimov.hrsystem.repository.DepartmentRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository repository;

    @Test
    void createRejectsBlankName() {
        DepartmentService service = new DepartmentService(repository);
        assertThrows(ValidationException.class, () -> service.create("   "));
    }

    @Test
    void createSavesAndReturnsDto() {
        when(repository.save(any(Department.class))).thenAnswer(invocation -> {
            Department department = invocation.getArgument(0);
            department.setId(1L);
            return department;
        });

        DepartmentService service = new DepartmentService(repository);
        DepartmentDto dto = service.create("Engineering");

        assertEquals(1L, dto.id());
        assertEquals("Engineering", dto.name());
    }
}
