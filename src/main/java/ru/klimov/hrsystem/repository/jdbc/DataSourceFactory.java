package ru.klimov.hrsystem.repository.jdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public final class DataSourceFactory {
    private DataSourceFactory() {
    }

    public static DataSource create() {
        HikariConfig config = new HikariConfig();
        // Tomcat's webapp classloader is invisible to DriverManager's ServiceLoader lookup.
        config.setDriverClassName("org.postgresql.Driver");
        config.setJdbcUrl(System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://db:5432/hrsystem"));
        config.setUsername(System.getenv().getOrDefault("DB_USER", "hrsystem"));
        config.setPassword(System.getenv().getOrDefault("DB_PASSWORD", "hrsystem"));
        config.setMaximumPoolSize(5);
        return new HikariDataSource(config);
    }
}
