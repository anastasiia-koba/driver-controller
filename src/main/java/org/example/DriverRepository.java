package org.example;
import java.util.List;

public interface DriverRepository {
    List<DriverState> readAllFromRepository();

    void writeToRepository(int driverId, int angle);

    record DriverState(int driverId, int angle) {}
}


