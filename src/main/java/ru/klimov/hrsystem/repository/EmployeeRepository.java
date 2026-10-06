package ru.klimov.hrsystem.repository;

import ru.klimov.hrsystem.model.Employee;
import ru.klimov.hrsystem.model.PositionType;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository {
    Employee save(Employee employee);
    Optional<Employee> findById(long id);
    List<Employee> findAll();
    List<Employee> findByDepartmentId(long departmentId);
    List<Employee> findByPositionType(PositionType type);
    void deleteById(long id);
}
