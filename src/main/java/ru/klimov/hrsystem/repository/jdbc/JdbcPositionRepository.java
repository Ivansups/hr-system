package ru.klimov.hrsystem.repository.jdbc;

import ru.klimov.hrsystem.model.*;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class JdbcPositionRepository implements ru.klimov.hrsystem.repository.PositionRepository {
    private final DataSource dataSource;

    public JdbcPositionRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Position save(Position position) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                if (position.getId() == null) {
                    insert(connection, position);
                } else {
                    update(connection, position);
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save position", e);
        }
        return position;
    }

    private void insert(Connection connection, Position position) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO positions (type, title, base_salary) VALUES (?, ?, ?) RETURNING id")) {
            statement.setString(1, position.getType().name());
            statement.setString(2, position.getTitle());
            statement.setBigDecimal(3, position.getBaseSalary());
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                position.setId(resultSet.getLong("id"));
            }
        }
        insertSubtype(connection, position);
    }

    private void update(Connection connection, Position position) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE positions SET title = ?, base_salary = ? WHERE id = ?")) {
            statement.setString(1, position.getTitle());
            statement.setBigDecimal(2, position.getBaseSalary());
            statement.setLong(3, position.getId());
            statement.executeUpdate();
        }
        updateSubtype(connection, position);
    }

    private void insertSubtype(Connection connection, Position position) throws SQLException {
        switch (position) {
            case DeveloperPosition d -> {
                try (PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO developer_positions (position_id, tech_stack, grade) VALUES (?, ?, ?)")) {
                    statement.setLong(1, d.getId());
                    statement.setString(2, d.getTechStack());
                    statement.setInt(3, d.getGrade());
                    statement.executeUpdate();
                }
            }
            case ManagerPosition m -> {
                try (PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO manager_positions (position_id, team_size) VALUES (?, ?)")) {
                    statement.setLong(1, m.getId());
                    statement.setInt(2, m.getTeamSize());
                    statement.executeUpdate();
                }
            }
            case SalespersonPosition s -> {
                try (PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO salesperson_positions (position_id, sales_percent) VALUES (?, ?)")) {
                    statement.setLong(1, s.getId());
                    statement.setBigDecimal(2, s.getSalesPercent());
                    statement.executeUpdate();
                }
            }
        }
    }

    private void updateSubtype(Connection connection, Position position) throws SQLException {
        switch (position) {
            case DeveloperPosition d -> {
                try (PreparedStatement statement = connection.prepareStatement(
                        "UPDATE developer_positions SET tech_stack = ?, grade = ? WHERE position_id = ?")) {
                    statement.setString(1, d.getTechStack());
                    statement.setInt(2, d.getGrade());
                    statement.setLong(3, d.getId());
                    statement.executeUpdate();
                }
            }
            case ManagerPosition m -> {
                try (PreparedStatement statement = connection.prepareStatement(
                        "UPDATE manager_positions SET team_size = ? WHERE position_id = ?")) {
                    statement.setInt(1, m.getTeamSize());
                    statement.setLong(2, m.getId());
                    statement.executeUpdate();
                }
            }
            case SalespersonPosition s -> {
                try (PreparedStatement statement = connection.prepareStatement(
                        "UPDATE salesperson_positions SET sales_percent = ? WHERE position_id = ?")) {
                    statement.setBigDecimal(1, s.getSalesPercent());
                    statement.setLong(2, s.getId());
                    statement.executeUpdate();
                }
            }
        }
    }

    @Override
    public Optional<Position> findById(long id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id, type, title, base_salary FROM positions WHERE id = ?")) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(loadWithSubtype(connection, resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find position", e);
        }
    }

    @Override
    public List<Position> findAll() {
        return runQuery("SELECT id, type, title, base_salary FROM positions ORDER BY id", null);
    }

    @Override
    public List<Position> findByType(PositionType type) {
        return runQuery("SELECT id, type, title, base_salary FROM positions WHERE type = ? ORDER BY id", type.name());
    }

    private List<Position> runQuery(String sql, String typeParam) {
        List<Position> result = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (typeParam != null) {
                statement.setString(1, typeParam);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(loadWithSubtype(connection, resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to query positions", e);
        }
        return result;
    }

    private Position loadWithSubtype(Connection connection, ResultSet baseRow) throws SQLException {
        long id = baseRow.getLong("id");
        PositionType type = PositionType.valueOf(baseRow.getString("type"));
        String title = baseRow.getString("title");
        BigDecimal baseSalary = baseRow.getBigDecimal("base_salary");

        Position position = switch (type) {
            case DEVELOPER -> {
                try (PreparedStatement statement = connection.prepareStatement(
                        "SELECT tech_stack, grade FROM developer_positions WHERE position_id = ?")) {
                    statement.setLong(1, id);
                    try (ResultSet subRow = statement.executeQuery()) {
                        subRow.next();
                        yield new DeveloperPosition(title, baseSalary, subRow.getString("tech_stack"), subRow.getInt("grade"));
                    }
                }
            }
            case MANAGER -> {
                try (PreparedStatement statement = connection.prepareStatement(
                        "SELECT team_size FROM manager_positions WHERE position_id = ?")) {
                    statement.setLong(1, id);
                    try (ResultSet subRow = statement.executeQuery()) {
                        subRow.next();
                        yield new ManagerPosition(title, baseSalary, subRow.getInt("team_size"));
                    }
                }
            }
            case SALESPERSON -> {
                try (PreparedStatement statement = connection.prepareStatement(
                        "SELECT sales_percent FROM salesperson_positions WHERE position_id = ?")) {
                    statement.setLong(1, id);
                    try (ResultSet subRow = statement.executeQuery()) {
                        subRow.next();
                        yield new SalespersonPosition(title, baseSalary, subRow.getBigDecimal("sales_percent"));
                    }
                }
            }
        };
        position.setId(id);
        return position;
    }

    @Override
    public void deleteById(long id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM positions WHERE id = ?")) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete position", e);
        }
    }
}
