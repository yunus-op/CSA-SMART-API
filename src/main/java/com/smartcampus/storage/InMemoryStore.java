package com.smartcampus.storage;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe in-memory data store for all Smart Campus entities.
 * 
 * Uses ConcurrentHashMap to ensure safe concurrent access from multiple
 * request-handling threads, since JAX-RS resource classes are instantiated
 * per-request by default.
 * 
 * This class follows the Singleton pattern — all data is stored in static
 * collections shared across the entire application.
 */
public class InMemoryStore {

    // Room storage: key = room ID, value = Room object
    private static final Map<String, Room> rooms = new ConcurrentHashMap<>();

    // Sensor storage: key = sensor ID, value = Sensor object
    private static final Map<String, Sensor> sensors = new ConcurrentHashMap<>();

    // Sensor reading history: key = sensor ID, value = list of readings
    private static final Map<String, List<SensorReading>> sensorReadings = new ConcurrentHashMap<>();

    // --- Room Operations ---

    public static Map<String, Room> getRooms() {
        return rooms;
    }

    public static Room getRoom(String id) {
        return rooms.get(id);
    }

    public static void addRoom(Room room) {
        rooms.put(room.getId(), room);
    }

    public static Room removeRoom(String id) {
        return rooms.remove(id);
    }

    // --- Sensor Operations ---

    public static Map<String, Sensor> getSensors() {
        return sensors;
    }

    public static Sensor getSensor(String id) {
        return sensors.get(id);
    }

    public static void addSensor(Sensor sensor) {
        sensors.put(sensor.getId(), sensor);
        // Initialize an empty readings list for this sensor
        sensorReadings.putIfAbsent(sensor.getId(), new ArrayList<>());
    }

    public static Sensor removeSensor(String id) {
        sensorReadings.remove(id);
        return sensors.remove(id);
    }

    // --- Sensor Reading Operations ---

    public static List<SensorReading> getReadings(String sensorId) {
        return sensorReadings.getOrDefault(sensorId, new ArrayList<>());
    }

    public static void addReading(String sensorId, SensorReading reading) {
        sensorReadings.computeIfAbsent(sensorId, k -> new ArrayList<>()).add(reading);
    }

    public static void seedData() {
        // Seed rooms
        Room room1 = new Room("LIB-301", "Library Quiet Study", 50);
        Room room2 = new Room("CS-101", "Computer Science Lab", 30);
        Room room3 = new Room("ENG-205", "Engineering Workshop", 40);
        addRoom(room1);
        addRoom(room2);
        addRoom(room3);

        // Seed sensors
        Sensor sensor1 = new Sensor("TEMP-001", "Temperature", "ACTIVE", 22.5, "LIB-301");
        Sensor sensor2 = new Sensor("CO2-001", "CO2", "ACTIVE", 400.0, "CS-101");
        Sensor sensor3 = new Sensor("OCC-001", "Occupancy", "MAINTENANCE", 0.0, "ENG-205");
        addSensor(sensor1);
        addSensor(sensor2);
        addSensor(sensor3);

        // Link sensors to rooms
        room1.getSensorIds().add(sensor1.getId());
        room2.getSensorIds().add(sensor2.getId());
        room3.getSensorIds().add(sensor3.getId());

        System.out.println("Data already loaded successfully!");
    }
}
