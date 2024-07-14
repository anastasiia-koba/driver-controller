package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class H2DriverRepository implements DriverRepository {

    private static String DB_URL = "jdbc:h2:./test";
    private static String DB_USER = "sa";
    private static String DB_PASSWORD = "";
    private static Logger logger = LoggerFactory.getLogger(H2DriverRepository.class);

    public void init() {

    }
    @Override
    public List<DriverState> readAllFromRepository() {
        List<DriverState> result = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT id, angle FROM drivers_data";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                Integer driverId = resultSet.getInt("id");
                Integer angle = resultSet.getInt("angle");
                result.add(new DriverState(driverId, angle));
            }
        } catch (SQLException e) {
            logger.error("Read all from db repository failed: ", e);
        }
        return result;
    }

    @Override
    public void writeToRepository(int driverId, int angle) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT angle FROM drivers_data WHERE id=? FOR UPDATE";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, driverId);
            ResultSet resultSet = statement.executeQuery();

            String updateSql;
            if (resultSet.next()) {
                updateSql = "UPDATE drivers_data SET angle=? WHERE id=?";
            } else {
                updateSql = "INSERT INTO drivers_data (angle, id) VALUES (?, ?)";
            }
            PreparedStatement update = connection.prepareStatement(updateSql);
            update.setInt(1, angle);
            update.setInt(2, driverId);
            update.executeUpdate();
        } catch (SQLException e) {
            logger.error("Writing to repository failed for driverId=" + driverId + " : ", e);
        }
    }
}
