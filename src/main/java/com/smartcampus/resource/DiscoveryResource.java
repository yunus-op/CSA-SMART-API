package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("/api/v1")
public class DiscoveryResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response discover() {

        Map<String, Object> response = new HashMap<>();
        response.put("api", "Smart Campus API");
        response.put("version", "v1");
        response.put("status", "running");
        response.put("contact", "admin@smartcampus.ac.uk");

        Map<String, String> links = new HashMap<>();
        links.put("rooms", "/api/v1/rooms");
        links.put("sensors", "/api/v1/sensors");
        response.put("resources", links);


        response.put("timestamp", System.currentTimeMillis());
        response.put("documentation", "See README.md for full API docs");

        return Response.ok(response).build();
    }
}