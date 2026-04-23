package com.smartcampus;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.store.DataStore;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;

public class Main {

    public static void main(String[] args) throws IOException {
        ResourceConfig config = new ResourceConfig()
                .packages(
                        "com.smartcampus.resource",
                        "com.smartcampus.config",
                        "com.smartcampus.exception",
                        "com.smartcampus.filter"
                )
                .register(JacksonFeature.class);

        HttpServer server = GrizzlyHttpServerFactory
                .createHttpServer(URI.create("http://0.0.0.0:8080/"), config);

        seedData();

        System.out.println("Server started at http://localhost:8080/");
        System.in.read();
        server.shutdownNow();
    }

    private static void seedData() {
        DataStore store = DataStore.getInstance();

        // Seed rooms
        Room room1 = new Room("LIB-301", "Library Quiet Study", 50);
        Room room2 = new Room("CS-101", "Computer Science Lab", 30);
        Room room3 = new Room("ENG-205", "Engineering Workshop", 40);
        store.getRooms().put(room1.getId(), room1);
        store.getRooms().put(room2.getId(), room2);
        store.getRooms().put(room3.getId(), room3);

        // Seed sensors
        Sensor sensor1 = new Sensor(
                "TEMP-001", "Temperature", "ACTIVE", 22.5, "LIB-301");
        Sensor sensor2 = new Sensor(
                "CO2-001", "CO2", "ACTIVE", 400.0, "CS-101");
        Sensor sensor3 = new Sensor(
                "OCC-001", "Occupancy", "MAINTENANCE", 0.0, "ENG-205");
        store.getSensors().put(sensor1.getId(), sensor1);
        store.getSensors().put(sensor2.getId(), sensor2);
        store.getSensors().put(sensor3.getId(), sensor3);

        // Link sensors to rooms
        room1.getSensorIds().add(sensor1.getId());
        room2.getSensorIds().add(sensor2.getId());
        room3.getSensorIds().add(sensor3.getId());

        System.out.println("Seed data loaded successfully!");
    }
}