/**
 * Resource class for managing Rooms.
 * Handles GET, POST, PUT, DELETE for /api/v1/rooms
 */
package com.smartcampus.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;
import com.smartcampus.store.DataStore;


import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;


@Path("/api/v1/rooms")
@Produces(MediaType.APPLICATION_JSON)
public class RoomResource {

    private final DataStore store = DataStore.getInstance();
    private final ObjectMapper mapper = new ObjectMapper();

    @GET
    public Response getAllRooms() {
        List<Room> rooms = new ArrayList<>(store.getRooms().values());
        return Response.ok(mapper.valueToTree(rooms).toString()).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createRoom(String body) {
        try {
            Room room = mapper.readValue(body, Room.class);

            if (room.getId() == null || room.getId().isEmpty()) {
                return Response.status(400)
                        .entity("{\"error\":\"Room ID is required\"}")
                        .build();
            }
            if (store.getRooms().containsKey(room.getId())) {
                return Response.status(409)
                        .entity("{\"error\":\"Room already exists\"}")
                        .build();
            }

            store.getRooms().put(room.getId(), room);
            return Response.status(201)
                    .entity(mapper.writeValueAsString(room))
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(400)
                    .entity("{\"error\":\"Invalid JSON: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {
        try {
            Room room = store.getRooms().get(roomId);
            if (room == null) {
                return Response.status(404)
                        .entity("{\"error\":\"Room not found: " + roomId + "\"}")
                        .build();
            }
            return Response.ok(mapper.writeValueAsString(room)).build();
        } catch (Exception e) {
            return Response.status(500)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = store.getRooms().get(roomId);
        if (room == null) {
            return Response.status(404)
                    .entity("{\"error\":\"Room not found: " + roomId + "\"}")
                    .build();
        }
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(roomId);
        }
        store.getRooms().remove(roomId);
        return Response.noContent().build();
    }

    // PUT /api/v1/rooms/{roomId} - update a room
    @PUT
    @Path("/{roomId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateRoom(
            @PathParam("roomId") String roomId, String body) {
        try {
            Room existing = store.getRooms().get(roomId);
            if (existing == null) {
                return Response.status(404)
                        .entity("{\"error\":\"Room not found: " + roomId + "\"}")
                        .build();
            }

            Room updated = mapper.readValue(body, Room.class);

            if (updated.getName() != null) {
                existing.setName(updated.getName());
            }
            if (updated.getCapacity() != 0) {
                existing.setCapacity(updated.getCapacity());
            }

            return Response.ok(mapper.writeValueAsString(existing)).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }
    @GET
    @Path("/crash")
    public Response crash() {
        String s = null;
        s.length();
        return Response.ok().build();
    }

}