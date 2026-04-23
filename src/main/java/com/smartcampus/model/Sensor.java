package com.smartcampus.model;

/**
 * POJO representing a sensor device deployed on the Smart Campus.
 * Each sensor is linked to a Room via its roomId foreign key.
 */
public class Sensor {

    private String id;              // Unique identifier, e.g., "TEMP-001"
    private String type;            // Category: "Temperature", "Occupancy", "CO2"
    private String status;          // Current state: "ACTIVE", "MAINTENANCE", or "OFFLINE"
    private double currentValue;    // The most recent measurement recorded
    private String roomId;          // Foreign key linking to the Room

    // Default constructor (required for JSON deserialization)
    public Sensor() {
    }

    // Parameterized constructor
    public Sensor(String id, String type, String status, double currentValue, String roomId) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.currentValue = currentValue;
        this.roomId = roomId;
    }

    // --- Getters and Setters ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(double currentValue) {
        this.currentValue = currentValue;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
}
