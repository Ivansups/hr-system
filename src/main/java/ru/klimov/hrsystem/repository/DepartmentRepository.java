package ru.klimov.hrsystem.repository;

import ru.klimov.hrsystem.model.Department;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository {
    Department save(Department department);
    Optional<Department> findById(long id);
    List<Department> findAll();
    void deleteById(long id);
}
