package com.smartcampus.resource;

import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;
import com.smartcampus.storage.InMemoryStore;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * JAX-RS Resource class for managing Rooms on the Smart Campus.
 * 
 * Handles CRUD operations at the /api/v1/rooms path.
 * 
 * Business Rule: A room cannot be deleted if it still has sensors assigned to it.
 * Attempting to do so triggers a RoomNotEmptyException (HTTP 409 Conflict).
 */
@Path("/api/v1/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    /**
     * GET /api/v1/rooms
     * Returns a comprehensive list of all rooms registered in the system.
     */
    @GET
    public Response getAllRooms() {
        // ORIGINAL CODE (Restored to return seeded data)
        List<Room> rooms = new ArrayList<>(InMemoryStore.getRooms().values());
        return Response.ok(rooms).build();
    }

    /**
     * GET /api/v1/rooms/{roomId}
     * Returns detailed metadata for a specific room identified by its ID.
     * 
     * @param roomId the unique room identifier
     * @return 200 OK with the room data, or 404 Not Found
     */
    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = InMemoryStore.getRoom(roomId);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorJson("Room not found", "No room exists with ID: " + roomId))
                    .build();
        }
        return Response.ok(room).build();
    }

    /**
     * POST /api/v1/rooms
     * Creates a new room in the system.
     * Returns 201 Created with a Location header pointing to the new resource.
     * 
     * @param room the room data from the JSON request body
     * @param uriInfo injected URI context for building the Location header
     * @return 201 Created with the room in the body
     */
    @POST
    public Response createRoom(Room room, @Context UriInfo uriInfo) {
        // Validate that the room has a required ID
        if (room.getId() == null || room.getId().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorJson("Validation Error", "Room ID is required and cannot be empty."))
                    .build();
        }

        // Check for duplicate room ID
        if (InMemoryStore.getRoom(room.getId()) != null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(errorJson("Duplicate Resource", "A room with ID '" + room.getId() + "' already exists."))
                    .build();
        }

        InMemoryStore.addRoom(room);

        // Build the Location header URI: /api/v1/rooms/{roomId}
        URI locationUri = uriInfo.getAbsolutePathBuilder().path(room.getId()).build();

        return Response.created(locationUri).entity(room).build();
    }

    /**
     * DELETE /api/v1/rooms/{roomId}
     * Decommissions a room from the system.
     * 
     * Business Rule: The room CANNOT be deleted if it still has sensors assigned.
     * This prevents orphaned sensor data. Throws RoomNotEmptyException (409).
     * 
     * Idempotency: If the room does not exist, returns 204 No Content (safe to retry).
     * 
     * @param roomId the unique room identifier
     * @return 204 No Content on success, or 409 Conflict if sensors remain
     */
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = InMemoryStore.getRoom(roomId);

        // If room doesn't exist, return 204 (idempotent — same result regardless of retries)
        if (room == null) {
            return Response.noContent().build();
        }

        // Business Logic Constraint: block deletion if sensors are still assigned
        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(
                    "Room '" + roomId + "' cannot be deleted because it still has " +
                    room.getSensorIds().size() + " sensor(s) assigned to it. " +
                    "Please reassign or remove all sensors before decommissioning this room."
            );
        }

        InMemoryStore.removeRoom(roomId);
        return Response.noContent().build();
    }

    /**
     * Helper method to create a structured JSON error object.
     */
    private java.util.Map<String, String> errorJson(String error, String message) {
        java.util.Map<String, String> err = new java.util.LinkedHashMap<>();
        err.put("error", error);
        err.put("message", message);
        return err;
    }
}
