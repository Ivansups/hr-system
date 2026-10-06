package ru.klimov.hrsystem.service;

import ru.klimov.hrsystem.dto.DepartmentDto;
import ru.klimov.hrsystem.exception.EntityNotFoundException;
import ru.klimov.hrsystem.exception.ValidationException;
import ru.klimov.hrsystem.mapper.DepartmentMapper;
import ru.klimov.hrsystem.model.Department;
import ru.klimov.hrsystem.repository.DepartmentRepository;

import java.util.List;

public final class DepartmentService {
    private final DepartmentRepository repository;

    public DepartmentService(DepartmentRepository repository) {
        this.repository = repository;
    }

    public DepartmentDto create(String name) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Department name must not be blank");
        }
        Department saved = repository.save(new Department(name));
        return DepartmentMapper.toDto(saved);
    }

    public DepartmentDto getById(long id) {
        Department department = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Department not found: id=" + id));
        return DepartmentMapper.toDto(department);
    }

    public List<DepartmentDto> getAll() {
        return repository.findAll().stream().map(DepartmentMapper::toDto).toList();
    }

    public void delete(long id) {
        repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Department not found: id=" + id));
        repository.deleteById(id);
    }
}
