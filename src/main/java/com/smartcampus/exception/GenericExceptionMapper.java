/**
 * Global exception mapper that catches all unhandled exceptions
 * and returns a clean 500 JSON response.
 */


package com.smartcampus.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER =
            Logger.getLogger(GenericExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable ex) {
        LOGGER.severe("Unexpected error: " + ex.getMessage());
        ex.printStackTrace();

        Map<String, String> error = new HashMap<>();
        error.put("error", "500 Internal Server Error");
        error.put("message", "An unexpected error occurred");
        error.put("hint", "Please contact the API administrator");

        return Response.status(500)
                .entity(error)
                .type("application/json")
                .build();
    }
}