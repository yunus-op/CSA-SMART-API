package com.smartcampus.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;

@Provider
public class LinkedResourceNotFoundExceptionMapper
        implements ExceptionMapper<LinkedResourceNotFoundException> {

    @Override
    public Response toResponse(LinkedResourceNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "422 Unprocessable Entity");
        error.put("message", ex.getMessage());
        error.put("resourceType", ex.getResourceType());
        error.put("resourceId", ex.getResourceId());
        error.put("hint",
                "The referenced resource does not exist in the system");

        return Response.status(422)
                .entity(error)
                .type("application/json")
                .build();
    }
}