/**
 * Resource class for managing Sensors.
 * Handles GET, POST, PUT, DELETE for /api/v1/sensors
 * Supports filtering by type via ?type= query parameter
 */
package com.smartcampus.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.Room;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/api/v1/sensors")
@Produces(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final DataStore store = DataStore.getInstance();
    private final ObjectMapper mapper = new ObjectMapper();

    // GET /api/v1/sensors - get all sensors with optional type filter
    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        try {
            List<Sensor> sensors = new ArrayList<>(store.getSensors().values());

            if (type != null && !type.isEmpty()) {
                List<Sensor> filtered = new ArrayList<>();
                for (Sensor s : sensors) {
                    if (s.getType() != null &&
                            s.getType().equalsIgnoreCase(type)) {
                        filtered.add(s);
                    }
                }
                return Response.ok(mapper.writeValueAsString(filtered)).build();
            }

            return Response.ok(mapper.writeValueAsString(sensors)).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    // POST /api/v1/sensors - create a new sensor
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createSensor(String body) {

        // Parse JSON outside try-catch
        Sensor sensor;
        try {
            sensor = mapper.readValue(body, Sensor.class);
        } catch (Exception e) {
            return Response.status(400)
                    .entity("{\"error\":\"Invalid JSON body\"}")
                    .build();
        }

        // All validation outside try-catch so exceptions reach mappers
        if (sensor.getId() == null || sensor.getId().isEmpty()) {
            return Response.status(400)
                    .entity("{\"error\":\"Sensor ID is required\"}")
                    .build();
        }

        if (store.getSensors().containsKey(sensor.getId())) {
            return Response.status(409)
                    .entity("{\"error\":\"Sensor already exists\"}")
                    .build();
        }

        if (sensor.getRoomId() == null || sensor.getRoomId().isEmpty()) {
            return Response.status(400)
                    .entity("{\"error\":\"Room ID is required\"}")
                    .build();
        }

        // This throw is now OUTSIDE try-catch — mapper will catch it
        Room room = store.getRooms().get(sensor.getRoomId());
        if (room == null) {
            throw new LinkedResourceNotFoundException("Room", sensor.getRoomId());
        }

        if (sensor.getStatus() == null || sensor.getStatus().isEmpty()) {
            sensor.setStatus("ACTIVE");
        }

        store.getSensors().put(sensor.getId(), sensor);
        room.getSensorIds().add(sensor.getId());

        try {
            return Response.status(201)
                    .entity(mapper.writeValueAsString(sensor))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    // GET /api/v1/sensors/{sensorId} - get a specific sensor
    @GET
    @Path("/{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId) {
        try {
            Sensor sensor = store.getSensors().get(sensorId);
            if (sensor == null) {
                return Response.status(404)
                        .entity("{\"error\":\"Sensor not found: " + sensorId + "\"}")
                        .build();
            }
            return Response.ok(mapper.writeValueAsString(sensor)).build();
        } catch (Exception e) {
            return Response.status(500)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    // DELETE /api/v1/sensors/{sensorId} - delete a sensor
    @DELETE
    @Path("/{sensorId}")
    public Response deleteSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensors().get(sensorId);
        if (sensor == null) {
            return Response.status(404)
                    .entity("{\"error\":\"Sensor not found: " + sensorId + "\"}")
                    .build();
        }

        // Unlink sensor from room
        Room room = store.getRooms().get(sensor.getRoomId());
        if (room != null) {
            room.getSensorIds().remove(sensorId);
        }

        store.getSensors().remove(sensorId);
        return Response.noContent().build();
    }

    // Sub-resource locator for /api/v1/sensors/{sensorId}/readings
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(
            @PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }

    // PUT /api/v1/sensors/{sensorId} - update sensor status
    @PUT
    @Path("/{sensorId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateSensor(
            @PathParam("sensorId") String sensorId, String body) {
        try {
            Sensor existing = store.getSensors().get(sensorId);
            if (existing == null) {
                return Response.status(404)
                        .entity("{\"error\":\"Sensor not found: " + sensorId + "\"}")
                        .build();
            }

            Sensor updated = mapper.readValue(body, Sensor.class);

            if (updated.getStatus() != null) {
                existing.setStatus(updated.getStatus());
            }
            if (updated.getType() != null) {
                existing.setType(updated.getType());
            }
            if (updated.getCurrentValue() != 0) {
                existing.setCurrentValue(updated.getCurrentValue());
            }

            return Response.ok(mapper.writeValueAsString(existing)).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }
}