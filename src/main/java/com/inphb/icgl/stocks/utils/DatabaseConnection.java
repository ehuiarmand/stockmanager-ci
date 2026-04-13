package com.inphb.icgl.stocks.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/stockmanager_ci?useSSL=false&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";

    private static Connection connection;
    private static String currentUrl;
    private static String currentUser;
    private static String currentPassword;

    private DatabaseConnection() {
    }

    public static synchronized Connection getConnection() throws SQLException {
        String url = System.getProperty("stockmanager.db.url",
                System.getenv().getOrDefault("STOCKMANAGER_DB_URL", DEFAULT_URL));
        String user = System.getProperty("stockmanager.db.user",
                System.getenv().getOrDefault("STOCKMANAGER_DB_USER", DEFAULT_USER));
        String password = System.getProperty("stockmanager.db.password",
                System.getenv().getOrDefault("STOCKMANAGER_DB_PASSWORD", DEFAULT_PASSWORD));

        boolean configChanged = !url.equals(currentUrl)
                || !user.equals(currentUser)
                || !password.equals(currentPassword);

        if (configChanged) {
            closeConnection();
        }

        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(url, user, password);
            currentUrl = url;
            currentUser = user;
            currentPassword = password;
        }
        return connection;
    }

    public static synchronized void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
        connection = null;
        currentUrl = null;
        currentUser = null;
        currentPassword = null;
    }
}
