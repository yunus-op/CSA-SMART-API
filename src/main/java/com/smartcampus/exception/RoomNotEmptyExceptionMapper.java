package com.smartcampus.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;

@Provider
public class RoomNotEmptyExceptionMapper
        implements ExceptionMapper<RoomNotEmptyException> {

    @Override
    public Response toResponse(RoomNotEmptyException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "409 Conflict");
        error.put("message", ex.getMessage());
        error.put("roomId", ex.getRoomId());
        error.put("hint", "Remove all sensors from this room before deleting it");

        return Response.status(Response.Status.CONFLICT)
                .entity(error)
                .type("application/json")
                .build();
    }
}