package com.smartcampus.resource;

import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.storage.InMemoryStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

/**
 * Sub-Resource class for managing historical readings of a specific sensor.
 * 
 * This class is NOT annotated with @Path at the class level — it is instantiated
 * and returned by the sub-resource locator method in SensorResource.
 * 
 * Each instance is scoped to a specific sensorId, passed via the constructor.
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;

    /**
     * Constructor — receives the parent sensor's ID from the sub-resource locator.
     * @param sensorId the ID of the sensor whose readings are being managed
     */
    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    /**
     * GET /api/v1/sensors/{sensorId}/readings
     * Returns the complete history of readings for this sensor.
     * 
     * @return 200 OK with list of SensorReading objects
     */
    @GET
    public Response getAllReadings() {
        List<SensorReading> readings = InMemoryStore.getReadings(sensorId);
        return Response.ok(readings).build();
    }

    /**
     * GET /api/v1/sensors/{sensorId}/readings/{readingId}
     * Returns a specific reading by its ID.
     * 
     * @param readingId the unique reading identifier
     * @return 200 OK with the reading, or 404 Not Found
     */
    @GET
    @Path("/{readingId}")
    public Response getReadingById(@PathParam("readingId") String readingId) {
        List<SensorReading> readings = InMemoryStore.getReadings(sensorId);
        for (SensorReading reading : readings) {
            if (reading.getId() != null && reading.getId().equals(readingId)) {
                return Response.ok(reading).build();
            }
        }
        return Response.status(Response.Status.NOT_FOUND)
                .entity(errorJson("Reading not found",
                        "No reading with ID '" + readingId + "' exists for sensor '" + sensorId + "'."))
                .build();
    }

    /**
     * POST /api/v1/sensors/{sensorId}/readings
     * Appends a new reading to this sensor's historical log.
     * 
     * State Constraint: If the parent sensor's status is "MAINTENANCE", the sensor
     * is physically disconnected and cannot accept new readings. A
     * SensorUnavailableException is thrown (HTTP 403 Forbidden).
     * 
     * Side Effect: On success, the parent Sensor's currentValue is updated to reflect
     * the latest reading, ensuring data consistency across the API.
     * 
     * @param reading the reading data from the JSON body
     * @return 201 Created with the reading (including auto-generated ID and timestamp)
     */
    @POST
    public Response addReading(SensorReading reading) {
        Sensor parentSensor = InMemoryStore.getSensor(sensorId);

        // State Constraint: block readings for sensors under maintenance
        if (parentSensor != null && "MAINTENANCE".equalsIgnoreCase(parentSensor.getStatus())) {
            throw new SensorUnavailableException(
                    "Sensor '" + sensorId + "' is currently in MAINTENANCE mode and cannot accept new readings. " +
                    "Please wait until the sensor is back ACTIVE before posting data."
            );
        }

        // Auto-generate a UUID for the reading if not provided
        if (reading.getId() == null || reading.getId().trim().isEmpty()) {
            reading.setId(UUID.randomUUID().toString());
        }

        // Auto-set the timestamp to current time if not provided
        if (reading.getTimestamp() <= 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        // Store the reading in the sensor's history
        InMemoryStore.addReading(sensorId, reading);

        // SIDE EFFECT: Update the parent sensor's currentValue
        if (parentSensor != null) {
            parentSensor.setCurrentValue(reading.getValue());
        }

        return Response.status(Response.Status.CREATED).entity(reading).build();
    }

    /**
     * Helper method to create a structured JSON error object.
     */
    private java.util.Map<String, String> errorJson(String error, String message) {
        java.util.Map<String, String> err = new java.util.LinkedHashMap<>();
        err.put("error", error);
        err.put("message", message);
        return err;
    }
}
