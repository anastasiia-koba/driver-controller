package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;
import java.io.BufferedReader;
import java.io.InputStreamReader;


public class H2LoadUtil {
    private static String DB_URL = "jdbc:h2:./test";
    private static String DB_USER = "sa";
    private static String DB_PASSWORD = "";
    private static Logger logger = LoggerFactory.getLogger(H2LoadUtil.class);
    private static String LOAD_SCRIPT_FILEPATH = "drivers-load.sql";

    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            Statement statement = connection.createStatement();
            InputStream inputStream = H2LoadUtil.class.getClassLoader().getResourceAsStream(LOAD_SCRIPT_FILEPATH);
            if (inputStream == null) {
                logger.error("SQL script file not found: " + LOAD_SCRIPT_FILEPATH);
                return;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                StringBuilder sql = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    sql.append(line);
                    if (line.trim().endsWith(";")) {
                        statement.execute(sql.toString());
                        sql = new StringBuilder();
                    }
                }
            } catch (IOException e) {
                logger.error("Error reading SQL script file: ", e);
            }
        } catch (SQLException e) {
            logger.error("Database error: ", e);
        }
    }
}
