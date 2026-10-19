package ru.klimov.hrsystem.web;

import org.junit.jupiter.api.Test;
import ru.klimov.hrsystem.dto.DepartmentDto;
import ru.klimov.hrsystem.exception.ValidationException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class JsonMapperTest {

    @Test
    void fromJsonRejectsLiteralNullBody() {
        assertThrows(ValidationException.class, () -> JsonMapper.fromJson("null", DepartmentDto.class));
    }
}
