package org.example;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class Control {

    private static Logger logger = LoggerFactory.getLogger(Control.class);
    private List<Driver> driverList = new ArrayList<>();
    private DriverRepository driverRepository = new H2DriverRepository();
    private static int COVERING = 70;
    private CompletableFuture<Boolean> previousCommand = null;

    private BiConsumer<Driver, Integer> moveRight = (driver, speedToRight) -> driver.startMovingRight(speedToRight);
    BiConsumer<Driver, Integer> moveLeft = (driver, speedToLeft) -> driver.startMovingLeft(speedToLeft);

    public Control() {
         List<DriverRepository.DriverState> driversStates = driverRepository.readAllFromRepository();
         List<CompletableFuture<Boolean>> driversMovement = new ArrayList<>();
         for (DriverRepository.DriverState driverState : driversStates) {
             Driver newDriver = new Driver(driverState.driverId());
             driversMovement.add(moveRight(driverState.angle(), driverState.angle(), newDriver));
             driverList.add(newDriver);
         }
         driversMovement.forEach(CompletableFuture::join);
    }

    // todo understand why we need both angle and speed here
    public CompletableFuture<Boolean> moveRight(int angle, int speed, Driver camera) {
        addMovement(speed, moveRight, camera);

        return previousCommand;
    }

    // todo understand why we need both angle and speed here
    public CompletableFuture<Boolean> moveLeft(int angle, int speed, Driver camera) {
        addMovement(speed, moveLeft, camera);

        return previousCommand;
    }



    /**
     * Returns a JSON object with a list of ranges within 360 degrees, indicating
     * which areas are covered and which are not, only when all cameras have stopped moving.
     */
    public JSONObject getView() {
        JSONObject jo = new JSONObject();
        JSONArray coveredRanges = new JSONArray();
        JSONArray uncoveredRanges = new JSONArray();

        for (Driver driver : driverList) {
            while (driver.isMoving()) {
                logger.debug("Waiting while driver(id=" + driver.getId() + ") stops");
            }
            int angle = driver.getAngle();
            JSONObject driverView = new JSONObject();
            int start = angle;
            int end = start + COVERING;
            if (end > 360) end %= 360;

            driverView.put("start", start);
            driverView.put("end", end);
            coveredRanges.put(driverView);
        }
        jo.put("coveredRanges", coveredRanges);
        jo.put("uncoveredRanges", uncoveredRanges);
        return jo;
    }

    private static boolean completeMovement(int speed, BiConsumer<Driver, Integer> move, Driver camera) {
        move.accept(camera, speed);
        while (camera.isMoving()) {}
        camera.stop();
        return true;
    }

    private void addMovement(int speed, BiConsumer<Driver, Integer> move, Driver camera) {
        if (previousCommand == null) {
            previousCommand = CompletableFuture.supplyAsync(() -> {
                driverRepository.writeToRepository(camera.getId(), camera.getAngle() + speed);
                return completeMovement(speed, move, camera);
            });
        } else {
            previousCommand.thenApply(result -> {
                driverRepository.writeToRepository(camera.getId(), camera.getAngle() + speed);
                return completeMovement(speed, move, camera);
            });
        }
    }

    public static void main(String[] args) {
        Control control = new Control();
        JSONObject firstView = control.getView();
        System.out.println(control.getView());

        Driver first = control.driverList.getFirst();
        Driver second = control.driverList.get(1);

        control.moveLeft(20, 10, first);
        control.moveRight(170, 20, second);

        System.out.println(control.getView());

        control.moveRight(20, 10, first);
        control.moveLeft(170, 20, second);

        JSONObject lastView = control.getView();
        System.out.println(lastView);
    }
}
