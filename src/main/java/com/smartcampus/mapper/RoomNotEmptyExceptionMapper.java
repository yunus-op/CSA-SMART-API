package com.smartcampus.mapper;

import com.smartcampus.exception.RoomNotEmptyException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JAX-RS ExceptionMapper for RoomNotEmptyException.
 *
 * Intercepts this exception and converts it into a structured HTTP 409 Conflict
 * response with a JSON body explaining that the room has active hardware assigned.
 *
 * The @Provider annotation registers this mapper with the JAX-RS runtime automatically.
 */
@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {

    @Override
    public Response toResponse(RoomNotEmptyException exception) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("status", 409);
        error.put("error", "Conflict");
        error.put("code", "ROOM_NOT_EMPTY");
        error.put("message", exception.getMessage());
        error.put("hint", "Remove or reassign all sensors from this room before attempting deletion.");

        return Response.status(Response.Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}
