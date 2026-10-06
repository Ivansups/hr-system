package ru.klimov.hrsystem.mapper;

import org.junit.jupiter.api.Test;
import ru.klimov.hrsystem.dto.PositionDto;
import ru.klimov.hrsystem.model.DeveloperPosition;
import ru.klimov.hrsystem.model.Position;
import ru.klimov.hrsystem.model.PositionType;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PositionMapperTest {

    @Test
    void roundTripsDeveloperPosition() {
        DeveloperPosition original = new DeveloperPosition("Backend Developer", new BigDecimal("2000"), "Java", 3);
        original.setId(1L);

        PositionDto dto = PositionMapper.toDto(original);
        assertEquals(PositionType.DEVELOPER, dto.type());
        assertEquals("Java", dto.techStack());
        assertEquals(3, dto.grade());
        assertNull(dto.teamSize());
        assertNull(dto.salesPercent());

        Position rebuilt = PositionMapper.toEntity(dto);
        assertInstanceOf(DeveloperPosition.class, rebuilt);
        assertEquals(1L, rebuilt.getId());
        assertEquals("Java", ((DeveloperPosition) rebuilt).getTechStack());
    }
}
