package com.andre.rinha.adapter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {

    public static Connection createConnection() {
        try {
            String databaseUrl = System.getenv("DATABASE_URL");
            String databaseUser = System.getenv("DATABASE_USER");
            String databasePassword = System.getenv("DATABASE_PASSWORD");

            return DriverManager.getConnection("jdbc:" + databaseUrl, databaseUser, databasePassword);
        } catch (SQLException e) {
            System.err.println("Connection failed: " + e.getMessage());
            return null;
        }
    }

}
