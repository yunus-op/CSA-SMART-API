package com.smartcampus.mapper;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Global "Safety Net" ExceptionMapper for ALL uncaught Throwable types.
 *
 * This catch-all mapper ensures the API is "leak-proof" — it intercepts any
 * unexpected runtime errors (NullPointerException, IndexOutOfBoundsException, etc.)
 * and returns a clean, generic HTTP 500 Internal Server Error response.
 *
 * SECURITY: Raw Java stack traces are NEVER exposed to the client.
 * The full exception is logged server-side for debugging, while the client
 * only receives a safe, generic error message.
 *
 * Cybersecurity rationale: Exposing stack traces reveals:
 * - Internal file paths and package structure (aids directory traversal attacks)
 * - Library names and versions (enables targeted known-vulnerability exploits)
 * - Business logic and internal method flow (aids reverse engineering)
 * - Database table/column names if ORMs are involved (aids SQL injection)
 */
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GenericExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {
        // Log the full exception server-side for debugging — NEVER send this to the client
        LOGGER.log(Level.SEVERE, "Unexpected internal server error: ", exception);

        Map<String, Object> error = new LinkedHashMap<>();
        error.put("status", 500);
        error.put("error", "Internal Server Error");
        error.put("code", "UNEXPECTED_ERROR");
        error.put("message", "An unexpected error occurred on the server. " +
                "Please contact the system administrator if this problem persists.");
        error.put("hint", "Reference the server logs for diagnostic details.");

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}
