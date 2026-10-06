package ru.klimov.hrsystem.repository.jdbc;

import ru.klimov.hrsystem.model.Department;
import ru.klimov.hrsystem.repository.DepartmentRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class JdbcDepartmentRepository implements DepartmentRepository {
    private final DataSource dataSource;

    public JdbcDepartmentRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Department save(Department department) {
        try (Connection connection = dataSource.getConnection()) {
            if (department.getId() == null) {
                insert(connection, department);
            } else {
                update(connection, department);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save department", e);
        }
        return department;
    }

    private void insert(Connection connection, Department department) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO departments (name) VALUES (?) RETURNING id")) {
            statement.setString(1, department.getName());
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                department.setId(resultSet.getLong("id"));
            }
        }
    }

    private void update(Connection connection, Department department) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE departments SET name = ? WHERE id = ?")) {
            statement.setString(1, department.getName());
            statement.setLong(2, department.getId());
            statement.executeUpdate();
        }
    }

    @Override
    public Optional<Department> findById(long id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id, name FROM departments WHERE id = ?")) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(toDepartment(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find department", e);
        }
    }

    @Override
    public List<Department> findAll() {
        List<Department> result = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT id, name FROM departments ORDER BY id");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                result.add(toDepartment(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list departments", e);
        }
        return result;
    }

    @Override
    public void deleteById(long id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM departments WHERE id = ?")) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete department", e);
        }
    }

    private Department toDepartment(ResultSet resultSet) throws SQLException {
        Department department = new Department(resultSet.getString("name"));
        department.setId(resultSet.getLong("id"));
        return department;
    }
}
