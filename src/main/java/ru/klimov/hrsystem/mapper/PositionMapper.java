package ru.klimov.hrsystem.mapper;

import ru.klimov.hrsystem.dto.PositionDto;
import ru.klimov.hrsystem.model.*;

public final class PositionMapper {
    private PositionMapper() {
    }

    public static PositionDto toDto(Position position) {
        return switch (position) {
            case DeveloperPosition d -> new PositionDto(d.getId(), PositionType.DEVELOPER, d.getTitle(),
                    d.getBaseSalary(), d.getTechStack(), d.getGrade(), null, null);
            case ManagerPosition m -> new PositionDto(m.getId(), PositionType.MANAGER, m.getTitle(),
                    m.getBaseSalary(), null, null, m.getTeamSize(), null);
            case SalespersonPosition s -> new PositionDto(s.getId(), PositionType.SALESPERSON, s.getTitle(),
                    s.getBaseSalary(), null, null, null, s.getSalesPercent());
        };
    }

    public static Position toEntity(PositionDto dto) {
        Position position = switch (dto.type()) {
            case DEVELOPER -> new DeveloperPosition(dto.title(), dto.baseSalary(), dto.techStack(), dto.grade());
            case MANAGER -> new ManagerPosition(dto.title(), dto.baseSalary(), dto.teamSize());
            case SALESPERSON -> new SalespersonPosition(dto.title(), dto.baseSalary(), dto.salesPercent());
        };
        position.setId(dto.id());
        return position;
    }
}
