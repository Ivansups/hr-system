package ru.klimov.hrsystem.repository.jdbc;

import org.junit.jupiter.api.Test;
import ru.klimov.hrsystem.model.*;
import ru.klimov.hrsystem.repository.PositionRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class JdbcPositionRepositoryTest extends JdbcTestSupport {

    private final PositionRepository repository = new JdbcPositionRepository(dataSource);

    @Test
    void savesAndReloadsDeveloperPositionWithItsOwnFields() {
        Position saved = repository.save(new DeveloperPosition("Backend Developer", new BigDecimal("2000.00"), "Java", 3));

        Optional<Position> found = repository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertInstanceOf(DeveloperPosition.class, found.get());
        assertEquals("Java", ((DeveloperPosition) found.get()).getTechStack());
        assertEquals(3, ((DeveloperPosition) found.get()).getGrade());
    }

    @Test
    void savesAndReloadsManagerAndSalespersonPositions() {
        Position manager = repository.save(new ManagerPosition("Team Lead", new BigDecimal("3000.00"), 5));
        Position salesperson = repository.save(new SalespersonPosition("Sales Manager", new BigDecimal("1500.00"), new BigDecimal("5.00")));

        assertEquals(5, ((ManagerPosition) repository.findById(manager.getId()).get()).getTeamSize());
        assertEquals(0, new BigDecimal("5.00").compareTo(
                ((SalespersonPosition) repository.findById(salesperson.getId()).get()).getSalesPercent()));
    }

    @Test
    void findByTypeReturnsOnlyMatchingSubtype() {
        repository.save(new DeveloperPosition("Backend Developer", new BigDecimal("2000.00"), "Java", 3));
        repository.save(new ManagerPosition("Team Lead", new BigDecimal("3000.00"), 5));

        List<Position> developers = repository.findByType(PositionType.DEVELOPER);

        assertEquals(1, developers.size());
        assertInstanceOf(DeveloperPosition.class, developers.get(0));
    }

    @Test
    void deleteByIdCascadesToTheSubtypeTable() {
        Position saved = repository.save(new DeveloperPosition("Backend Developer", new BigDecimal("2000.00"), "Java", 3));
        repository.deleteById(saved.getId());
        assertTrue(repository.findById(saved.getId()).isEmpty());
    }
}
