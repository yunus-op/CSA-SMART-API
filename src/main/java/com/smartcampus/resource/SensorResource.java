package com.smartcampus.resource;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.storage.InMemoryStore;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JAX-RS Resource class for managing Sensors on the Smart Campus.
 * 
 * Handles operations at /api/v1/sensors and includes a sub-resource locator
 * that delegates /sensors/{sensorId}/readings to SensorReadingResource.
 */
@Path("/api/v1/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    /**
     * GET /api/v1/sensors
     * Returns all sensors. Supports optional filtering by sensor type via query parameter.
     * 
     * Example: GET /api/v1/sensors?type=CO2
     * 
     * @param type optional query parameter to filter sensors by type (case-insensitive)
     * @return 200 OK with list of sensors (filtered or full)
     */
    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        List<Sensor> sensors = new ArrayList<>(InMemoryStore.getSensors().values());

        // If the 'type' query parameter is provided, filter the list
        if (type != null && !type.trim().isEmpty()) {
            sensors = sensors.stream()
                    .filter(s -> s.getType() != null && s.getType().equalsIgnoreCase(type.trim()))
                    .collect(Collectors.toList());
        }

        return Response.ok(sensors).build();
    }

    /**
     * GET /api/v1/sensors/{sensorId}
     * Returns detailed information about a specific sensor.
     * 
     * @param sensorId the unique sensor identifier
     * @return 200 OK with sensor data, or 404 Not Found
     */
    @GET
    @Path("/{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = InMemoryStore.getSensor(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorJson("Sensor not found", "No sensor exists with ID: " + sensorId))
                    .build();
        }
        return Response.ok(sensor).build();
    }

    /**
     * POST /api/v1/sensors
     * Registers a new sensor in the system.
     * 
     * Dependency Validation: The roomId specified in the request body MUST reference
     * an existing Room. If the room does not exist, a LinkedResourceNotFoundException
     * is thrown (HTTP 422 Unprocessable Entity).
     * 
     * Side Effect: The sensor's ID is also added to the parent Room's sensorIds list.
     * 
     * @param sensor the sensor data from the JSON body
     * @param uriInfo injected URI context for building the Location header
     * @return 201 Created with sensor data
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor sensor, @Context UriInfo uriInfo) {
        // Validate sensor ID
        if (sensor.getId() == null || sensor.getId().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorJson("Validation Error", "Sensor ID is required and cannot be empty."))
                    .build();
        }

        // Check for duplicate sensor ID
        if (InMemoryStore.getSensor(sensor.getId()) != null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(errorJson("Duplicate Resource", "A sensor with ID '" + sensor.getId() + "' already exists."))
                    .build();
        }

        // Dependency Validation: verify the referenced Room exists
        if (sensor.getRoomId() == null || sensor.getRoomId().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorJson("Validation Error", "Room ID (roomId) is required for sensor registration."))
                    .build();
        }

        Room parentRoom = InMemoryStore.getRoom(sensor.getRoomId());
        if (parentRoom == null) {
            throw new LinkedResourceNotFoundException(
                    "The specified roomId '" + sensor.getRoomId() + "' does not reference an existing room. " +
                    "Please create the room first or provide a valid roomId."
            );
        }

        // Register the sensor
        InMemoryStore.addSensor(sensor);

        // Link the sensor to the parent room
        parentRoom.getSensorIds().add(sensor.getId());

        // Build location URI
        URI locationUri = uriInfo.getAbsolutePathBuilder().path(sensor.getId()).build();

        return Response.created(locationUri).entity(sensor).build();
    }

    /**
     * Sub-Resource Locator for /api/v1/sensors/{sensorId}/readings
     * 
     * Delegates all requests under the /readings sub-path to the
     * SensorReadingResource class. This pattern keeps the code modular
     * and separates concerns — reading management logic lives in its own class.
     * 
     * @param sensorId the parent sensor's ID, passed to the sub-resource
     * @return a new SensorReadingResource instance scoped to this sensor
     */
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingsSubResource(@PathParam("sensorId") String sensorId) {
        // Verify the sensor exists before delegating
        Sensor sensor = InMemoryStore.getSensor(sensorId);
        if (sensor == null) {
            throw new NotFoundException("Sensor with ID '" + sensorId + "' not found.");
        }
        return new SensorReadingResource(sensorId);
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
