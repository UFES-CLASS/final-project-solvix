package com.stockin.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    private static final String URL =
            "jdbc:sqlite:database/stockin.db";

    private static Connection connection;

    public static Connection getConnection() {

        try {

            if (connection == null || connection.isClosed()) {

                connection = DriverManager.getConnection(URL);

                try (Statement pragma = connection.createStatement()) {
                    pragma.execute("PRAGMA foreign_keys = ON;");
                }

                System.out.println("Database Connected");

            }

        } catch (SQLException e) {

            e.printStackTrace();

        }

        return connection;

    }

}