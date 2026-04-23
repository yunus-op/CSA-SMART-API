/**
 * Sub-resource class for managing SensorReadings.
 * Handles GET and POST for /api/v1/sensors/{sensorId}/readings
 */
package com.smartcampus.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

@Produces(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;
    private final DataStore store = DataStore.getInstance();
    private final ObjectMapper mapper = new ObjectMapper();

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    // GET /api/v1/sensors/{sensorId}/readings
    @GET
    public Response getReadings() {
        try {
            // Check sensor exists
            Sensor sensor = store.getSensors().get(sensorId);
            if (sensor == null) {
                return Response.status(404)
                        .entity("{\"error\":\"Sensor not found: " + sensorId + "\"}")
                        .build();
            }

            List<SensorReading> readings =
                    store.getReadingsForSensor(sensorId);
            return Response.ok(
                    mapper.writeValueAsString(readings)).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    // POST /api/v1/sensors/{sensorId}/readings
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addReading(String body) {

        // Check sensor exists
        Sensor sensor = store.getSensors().get(sensorId);
        if (sensor == null) {
            return Response.status(404)
                    .entity("{\"error\":\"Sensor not found: " + sensorId + "\"}")
                    .build();
        }

        // Check sensor status BEFORE try-catch so exception reaches the mapper
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus()) ||
                "OFFLINE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(sensorId, sensor.getStatus());
        }

        try {
            SensorReading reading = mapper.readValue(body, SensorReading.class);

            if (reading.getId() == null || reading.getId().isEmpty()) {
                reading.setId(UUID.randomUUID().toString());
            }

            if (reading.getTimestamp() == 0) {
                reading.setTimestamp(System.currentTimeMillis());
            }

            store.getReadingsForSensor(sensorId).add(reading);
            sensor.setCurrentValue(reading.getValue());

            return Response.status(201)
                    .entity(mapper.writeValueAsString(reading))
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }
    // GET /api/v1/sensors/{sensorId}/readings/{readingId}
    @GET
    @Path("/{readingId}")
    public Response getReadingById(
            @PathParam("readingId") String readingId) {
        try {
            Sensor sensor = store.getSensors().get(sensorId);
            if (sensor == null) {
                return Response.status(404)
                        .entity("{\"error\":\"Sensor not found: " + sensorId + "\"}")
                        .build();
            }

            List<SensorReading> readings =
                    store.getReadingsForSensor(sensorId);
            for (SensorReading r : readings) {
                if (r.getId().equals(readingId)) {
                    return Response.ok(
                            mapper.writeValueAsString(r)).build();
                }
            }

            return Response.status(404)
                    .entity("{\"error\":\"Reading not found: " + readingId + "\"}")
                    .build();

        } catch (Exception e) {
            return Response.status(500)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }
}