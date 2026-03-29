package com.restaurant.minidish.repository;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Component
public class DataSource {

    private final String url;
    private final String username;
    private final String password;

    public DataSource() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        this.url      = dotenv.get("JDBC_URL",    System.getenv("JDBC_URL"));
        this.username = dotenv.get("DB_USERNAME", System.getenv("DB_USERNAME"));
        this.password = dotenv.get("DB_PASSWORD", System.getenv("DB_PASSWORD"));
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
}