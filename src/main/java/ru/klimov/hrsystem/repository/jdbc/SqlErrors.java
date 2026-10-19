package ru.klimov.hrsystem.repository.jdbc;

import ru.klimov.hrsystem.exception.ConflictException;
import ru.klimov.hrsystem.exception.ValidationException;

import java.sql.SQLException;

final class SqlErrors {
    private static final String FOREIGN_KEY_VIOLATION = "23503";
    private static final String NOT_NULL_VIOLATION = "23502";
    private static final String CHECK_VIOLATION = "23514";
    private static final String DATA_EXCEPTION_CLASS = "22";

    private SqlErrors() {
    }

    /** Maps client-caused database errors to 4xx domain exceptions; anything else stays a 500. */
    static RuntimeException translate(SQLException e, String failureMessage) {
        String state = e.getSQLState();
        if (FOREIGN_KEY_VIOLATION.equals(state)) {
            return new ConflictException("Operation violates a reference to another record");
        }
        if (state != null && (state.startsWith(DATA_EXCEPTION_CLASS)
                || NOT_NULL_VIOLATION.equals(state) || CHECK_VIOLATION.equals(state))) {
            return new ValidationException("Invalid data: " + e.getMessage());
        }
        return new RuntimeException(failureMessage, e);
    }
}
