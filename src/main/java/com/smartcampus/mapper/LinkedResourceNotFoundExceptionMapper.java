package com.smartcampus.mapper;

import com.smartcampus.exception.LinkedResourceNotFoundException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JAX-RS ExceptionMapper for LinkedResourceNotFoundException.
 *
 * Returns HTTP 422 Unprocessable Entity when a client submits a request
 * with a valid JSON payload, but the payload references a resource (roomId)
 * that does not exist in the system.
 *
 * 422 is semantically more accurate than 404 here because:
 * - The REQUEST itself was found and parsed correctly (not a missing endpoint).
 * - The CONTENT of the request is the problem — a reference to a non-existent resource.
 * - 404 implies the requested URL/resource was not found, not an internal reference error.
 */
@Provider
public class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {

    @Override
    public Response toResponse(LinkedResourceNotFoundException exception) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("status", 422);
        error.put("error", "Unprocessable Entity");
        error.put("code", "LINKED_RESOURCE_NOT_FOUND");
        error.put("message", exception.getMessage());
        error.put("hint", "Ensure the referenced roomId exists before registering a sensor.");

        return Response.status(422)   // 422 Unprocessable Entity
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}
