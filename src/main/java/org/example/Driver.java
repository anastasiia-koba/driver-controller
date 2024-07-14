package org.example;

import java.util.concurrent.locks.ReentrantLock;

class Driver {

    private int id;
    private int angle;
    private boolean moving;
    private final ReentrantLock lock;

    public Driver(int id) {
        this.id = id;
        this.angle = 0;
        this.moving = false;
        this.lock = new ReentrantLock();
    }

    public void startMovingRight(int speed) {
        new Thread(() -> {
            lock.lock();
            try {
                moving = true;
                int targetAngle = angle + speed;
                while (angle < targetAngle) {
                    Thread.sleep(100); // Simulate time taken to move
                    angle = (angle + 1) % 360;
                }
                moving = false;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
            }
        }).start();
    }

    public void startMovingLeft(int speed) {
        new Thread(() -> {
            lock.lock();
            try {
                moving = true;
                int targetAngle = angle - speed;
                while (angle > targetAngle) {
                    Thread.sleep(100); // Simulate time taken to move
                    angle = (angle - 1 + 360) % 360;
                }
                moving = false;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
            }
        }).start();
    }

    public void stop() {
        lock.lock();
        try {
            moving = false;
        } finally {
            lock.unlock();
        }
    }

    public void moveHome() {
        new Thread(() -> {
            lock.lock();
            try {
                moving = true;
                angle = 0;
                moving = false;
            } finally {
                lock.unlock();
            }
        }).start();
    }

    public boolean isMoving() {
        lock.lock();
        try {
            return moving;
        } finally {
            lock.unlock();
        }
    }

    public int getAngle() {
        lock.lock();
        try {
            return angle;
        } finally {
            lock.unlock();
        }
    }

    public int getId() {
        return id;
    }

    public static void main(String[] args) throws InterruptedException {
        Driver camera = new Driver(1);

        System.out.println("Initial angle: " + camera.getAngle());

        // Test moving right
        System.out.println("Moving right by 30 degrees...");
        camera.startMovingRight(30);
        Thread.sleep(4000);  // Wait for the camera to finish moving
        System.out.println("Current angle after moving right: " + camera.getAngle());

        // Test moving left
        System.out.println("Moving left by 20 degrees...");
        camera.startMovingLeft(20);
        Thread.sleep(3000);  // Wait for the camera to finish moving
        System.out.println("Current angle after moving left: " + camera.getAngle());

        // Test stop
        System.out.println("Stopping camera...");
        camera.stop();
        System.out.println("Camera stopped. Is moving: " + camera.isMoving());

        // Test move home
        System.out.println("Moving camera to home position...");
        camera.moveHome();
        Thread.sleep(2000);  // Wait for the camera to finish moving
        System.out.println("Current angle after moving home: " + camera.getAngle());
    }
}
