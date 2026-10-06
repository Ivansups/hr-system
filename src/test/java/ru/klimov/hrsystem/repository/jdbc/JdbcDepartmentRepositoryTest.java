package ru.klimov.hrsystem.repository.jdbc;

import org.junit.jupiter.api.Test;
import ru.klimov.hrsystem.model.Department;
import ru.klimov.hrsystem.repository.DepartmentRepository;

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
}
