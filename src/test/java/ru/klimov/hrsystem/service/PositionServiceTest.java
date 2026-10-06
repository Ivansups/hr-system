package ru.klimov.hrsystem.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.klimov.hrsystem.dto.PositionDto;
import ru.klimov.hrsystem.exception.ValidationException;
import ru.klimov.hrsystem.model.Position;
import ru.klimov.hrsystem.model.PositionType;
import ru.klimov.hrsystem.repository.PositionRepository;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PositionServiceTest {

    @Mock
    private PositionRepository repository;

    @Test
    void createDeveloperWithoutTechStackFailsValidation() {
        PositionService service = new PositionService(repository);
        PositionDto invalid = new PositionDto(null, PositionType.DEVELOPER, "Backend Developer",
                new BigDecimal("2000"), null, null, null, null);

        assertThrows(ValidationException.class, () -> service.create(invalid));
    }

    @Test
    void createDeveloperWithRequiredFieldsSucceeds() {
        when(repository.save(any(Position.class))).thenAnswer(invocation -> {
            Position position = invocation.getArgument(0);
            position.setId(1L);
            return position;
        });

        PositionService service = new PositionService(repository);
        PositionDto valid = new PositionDto(null, PositionType.DEVELOPER, "Backend Developer",
                new BigDecimal("2000"), "Java", 3, null, null);

        PositionDto created = service.create(valid);

        assertEquals(1L, created.id());
        assertEquals("Java", created.techStack());
    }
}
