package ru.klimov.hrsystem.repository.jdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;

@Testcontainers
public abstract class JdbcTestSupport {

    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("hrsystem_test")
                    .withUsername("test")
                    .withPassword("test");

    protected static DataSource dataSource;

    @BeforeAll
    static void startContainerAndApplySchema() throws Exception {
        if (dataSource != null) {
            return;
        }
        POSTGRES.start();

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(POSTGRES.getJdbcUrl());
        config.setUsername(POSTGRES.getUsername());
        config.setPassword(POSTGRES.getPassword());
        dataSource = new HikariDataSource(config);

        String schema = Files.readString(Path.of("src/main/resources/db/schema.sql"));
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(schema);
        }
    }

    @AfterEach
    void cleanTables() throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    TRUNCATE employees, developer_positions, manager_positions,
                             salesperson_positions, positions, departments
                    RESTART IDENTITY CASCADE
                    """);
        }
    }
}
