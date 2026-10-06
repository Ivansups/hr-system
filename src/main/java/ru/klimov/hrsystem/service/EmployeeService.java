package ru.klimov.hrsystem.service;

import ru.klimov.hrsystem.dto.EmployeeDto;
import ru.klimov.hrsystem.exception.EntityNotFoundException;
import ru.klimov.hrsystem.exception.ValidationException;
import ru.klimov.hrsystem.mapper.EmployeeMapper;
import ru.klimov.hrsystem.model.Department;
import ru.klimov.hrsystem.model.Employee;
import ru.klimov.hrsystem.model.Position;
import ru.klimov.hrsystem.model.PositionType;
import ru.klimov.hrsystem.repository.DepartmentRepository;
import ru.klimov.hrsystem.repository.EmployeeRepository;
import ru.klimov.hrsystem.repository.PositionRepository;

import java.time.LocalDate;
import java.util.List;

public final class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;

    public EmployeeService(EmployeeRepository employeeRepository,
                            DepartmentRepository departmentRepository,
                            PositionRepository positionRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.positionRepository = positionRepository;
    }

    public EmployeeDto create(EmployeeDto dto) {
        validate(dto);
        Department department = resolveDepartment(dto.departmentId());
        Position position = resolvePosition(dto.positionId());
        Employee employee = new Employee(dto.fullName(), dto.hireDate(), department, position);
        return EmployeeMapper.toDto(employeeRepository.save(employee));
    }

    public EmployeeDto update(long id, EmployeeDto dto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found: id=" + id));
        validate(dto);
        employee.setFullName(dto.fullName());
        employee.setHireDate(dto.hireDate());
        employee.setDepartment(resolveDepartment(dto.departmentId()));
        employee.setPosition(resolvePosition(dto.positionId()));
        return EmployeeMapper.toDto(employeeRepository.save(employee));
    }

    public EmployeeDto getById(long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found: id=" + id));
        return EmployeeMapper.toDto(employee);
    }

    public List<EmployeeDto> getAll() {
        return employeeRepository.findAll().stream().map(EmployeeMapper::toDto).toList();
    }

    public List<EmployeeDto> getByDepartment(long departmentId) {
        return employeeRepository.findByDepartmentId(departmentId).stream().map(EmployeeMapper::toDto).toList();
    }

    public List<EmployeeDto> getByPositionType(PositionType type) {
        return employeeRepository.findByPositionType(type).stream().map(EmployeeMapper::toDto).toList();
    }

    public void delete(long id) {
        employeeRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Employee not found: id=" + id));
        employeeRepository.deleteById(id);
    }

    private void validate(EmployeeDto dto) {
        if (dto.fullName() == null || dto.fullName().isBlank()) {
            throw new ValidationException("Employee fullName must not be blank");
        }
        if (dto.hireDate() == null) {
            throw new ValidationException("Employee hireDate is required");
        }
        if (dto.hireDate().isAfter(LocalDate.now())) {
            throw new ValidationException("Employee hireDate must not be in the future");
        }
    }

    private Department resolveDepartment(Long departmentId) {
        if (departmentId == null) {
            throw new ValidationException("Employee departmentId is required");
        }
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new EntityNotFoundException("Department not found: id=" + departmentId));
    }

    private Position resolvePosition(Long positionId) {
        if (positionId == null) {
            throw new ValidationException("Employee positionId is required");
        }
        return positionRepository.findById(positionId)
                .orElseThrow(() -> new EntityNotFoundException("Position not found: id=" + positionId));
    }
}
