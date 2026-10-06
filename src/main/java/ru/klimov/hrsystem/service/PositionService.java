package ru.klimov.hrsystem.service;

import ru.klimov.hrsystem.dto.PositionDto;
import ru.klimov.hrsystem.exception.EntityNotFoundException;
import ru.klimov.hrsystem.exception.ValidationException;
import ru.klimov.hrsystem.mapper.PositionMapper;
import ru.klimov.hrsystem.model.Position;
import ru.klimov.hrsystem.repository.PositionRepository;

import java.math.BigDecimal;
import java.util.List;

public final class PositionService {
    private final PositionRepository repository;

    public PositionService(PositionRepository repository) {
        this.repository = repository;
    }

    public PositionDto create(PositionDto dto) {
        validate(dto);
        Position saved = repository.save(PositionMapper.toEntity(dto));
        return PositionMapper.toDto(saved);
    }

    public PositionDto getById(long id) {
        Position position = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Position not found: id=" + id));
        return PositionMapper.toDto(position);
    }

    public List<PositionDto> getAll() {
        return repository.findAll().stream().map(PositionMapper::toDto).toList();
    }

    public void delete(long id) {
        repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Position not found: id=" + id));
        repository.deleteById(id);
    }

    private void validate(PositionDto dto) {
        if (dto.title() == null || dto.title().isBlank()) {
            throw new ValidationException("Position title must not be blank");
        }
        if (dto.baseSalary() == null || dto.baseSalary().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Position baseSalary must be zero or positive");
        }
        switch (dto.type()) {
            case DEVELOPER -> {
                if (dto.techStack() == null || dto.techStack().isBlank() || dto.grade() == null) {
                    throw new ValidationException("Developer position requires techStack and grade");
                }
            }
            case MANAGER -> {
                if (dto.teamSize() == null) {
                    throw new ValidationException("Manager position requires teamSize");
                }
            }
            case SALESPERSON -> {
                if (dto.salesPercent() == null) {
                    throw new ValidationException("Salesperson position requires salesPercent");
                }
            }
        }
    }
}
