package ru.klimov.hrsystem.repository;

import ru.klimov.hrsystem.model.Position;
import ru.klimov.hrsystem.model.PositionType;

import java.util.List;
import java.util.Optional;

public interface PositionRepository {
    Position save(Position position);
    Optional<Position> findById(long id);
    List<Position> findAll();
    List<Position> findByType(PositionType type);
    void deleteById(long id);
}
