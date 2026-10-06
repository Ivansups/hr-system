package ru.klimov.hrsystem.repository.jdbc;

import ru.klimov.hrsystem.model.Department;
import ru.klimov.hrsystem.model.Employee;
import ru.klimov.hrsystem.model.Position;
import ru.klimov.hrsystem.model.PositionType;
import ru.klimov.hrsystem.repository.DepartmentRepository;
import ru.klimov.hrsystem.repository.EmployeeRepository;
import ru.klimov.hrsystem.repository.PositionRepository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class JdbcEmployeeRepository implements EmployeeRepository {
    private final DataSource dataSource;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;

    public JdbcEmployeeRepository(DataSource dataSource,
                                   DepartmentRepository departmentRepository,
                                   PositionRepository positionRepository) {
        this.dataSource = dataSource;
        this.departmentRepository = departmentRepository;
        this.positionRepository = positionRepository;
    }

    @Override
    public Employee save(Employee employee) {
        try (Connection connection = dataSource.getConnection()) {
            if (employee.getId() == null) {
                insert(connection, employee);
            } else {
                update(connection, employee);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save employee", e);
        }
        return employee;
    }

    private void insert(Connection connection, Employee employee) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO employees (full_name, hire_date, department_id, position_id) VALUES (?, ?, ?, ?) RETURNING id")) {
            statement.setString(1, employee.getFullName());
            statement.setDate(2, Date.valueOf(employee.getHireDate()));
            statement.setLong(3, employee.getDepartment().getId());
            statement.setLong(4, employee.getPosition().getId());
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                employee.setId(resultSet.getLong("id"));
            }
        }
    }

    private void update(Connection connection, Employee employee) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE employees SET full_name = ?, hire_date = ?, department_id = ?, position_id = ? WHERE id = ?")) {
            statement.setString(1, employee.getFullName());
            statement.setDate(2, Date.valueOf(employee.getHireDate()));
            statement.setLong(3, employee.getDepartment().getId());
            statement.setLong(4, employee.getPosition().getId());
            statement.setLong(5, employee.getId());
            statement.executeUpdate();
        }
    }

    @Override
    public Optional<Employee> findById(long id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id, full_name, hire_date, department_id, position_id FROM employees WHERE id = ?")) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(hydrate(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find employee", e);
        }
    }

    @Override
    public List<Employee> findAll() {
        return runQuery("SELECT id, full_name, hire_date, department_id, position_id FROM employees ORDER BY id", null, null);
    }

    @Override
    public List<Employee> findByDepartmentId(long departmentId) {
        return runQuery(
                "SELECT id, full_name, hire_date, department_id, position_id FROM employees WHERE department_id = ? ORDER BY id",
                departmentId, null);
    }

    @Override
    public List<Employee> findByPositionType(PositionType type) {
        String sql = """
                SELECT e.id, e.full_name, e.hire_date, e.department_id, e.position_id
                FROM employees e JOIN positions p ON p.id = e.position_id
                WHERE p.type = ? ORDER BY e.id
                """;
        return runQuery(sql, null, type.name());
    }

    private List<Employee> runQuery(String sql, Long longParam, String stringParam) {
        List<Employee> result = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (longParam != null) {
                statement.setLong(1, longParam);
            } else if (stringParam != null) {
                statement.setString(1, stringParam);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(hydrate(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to query employees", e);
        }
        return result;
    }

    private Employee hydrate(ResultSet resultSet) throws SQLException {
        Department department = departmentRepository.findById(resultSet.getLong("department_id"))
                .orElseThrow(() -> new IllegalStateException("Dangling department_id in employees table"));
        Position position = positionRepository.findById(resultSet.getLong("position_id"))
                .orElseThrow(() -> new IllegalStateException("Dangling position_id in employees table"));
        Employee employee = new Employee(resultSet.getString("full_name"),
                resultSet.getDate("hire_date").toLocalDate(), department, position);
        employee.setId(resultSet.getLong("id"));
        return employee;
    }

    @Override
    public void deleteById(long id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM employees WHERE id = ?")) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete employee", e);
        }
    }
}
