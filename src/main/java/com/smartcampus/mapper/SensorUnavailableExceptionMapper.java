package com.smartcampus.mapper;

import com.smartcampus.exception.SensorUnavailableException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JAX-RS ExceptionMapper for SensorUnavailableException.
 *
 * Returns HTTP 403 Forbidden when a POST to a sensor's readings endpoint
 * is attempted while the sensor is in MAINTENANCE status.
 *
 * 403 Forbidden is appropriate here because the client is authenticated and
 * the endpoint exists — but the server is refusing the action due to the
 * current state of the target resource.
 */
@Provider
public class SensorUnavailableExceptionMapper implements ExceptionMapper<SensorUnavailableException> {

    @Override
    public Response toResponse(SensorUnavailableException exception) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("status", 403);
        error.put("error", "Forbidden");
        error.put("code", "SENSOR_UNAVAILABLE");
        error.put("message", exception.getMessage());
        error.put("hint", "Check the sensor status. Only ACTIVE sensors can accept new readings.");

        return Response.status(Response.Status.FORBIDDEN)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}
