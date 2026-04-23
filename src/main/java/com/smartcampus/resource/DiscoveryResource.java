package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Discovery endpoint at the API root.
 * 
 * Returns essential API metadata: version info, administrative contact,
 * and a map of available resource collection URIs (HATEOAS-style navigation).
 */
@Path("/api/v1")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Response getApiInfo() {
        Map<String, Object> apiInfo = new LinkedHashMap<>();

        // API versioning information
        apiInfo.put("title", "Smart Campus Sensor & Room Management API");
        apiInfo.put("version", "1.0.0");
        apiInfo.put("description", "RESTful API for managing campus rooms, sensors, and their historical readings.");

        // Administrative contact details
        Map<String, String> contact = new LinkedHashMap<>();
        contact.put("name", "Smart Campus Admin");
        contact.put("email", "admin@smartcampus.university.ac.uk");
        contact.put("department", "Campus Facilities Management");
        apiInfo.put("contact", contact);

        // HATEOAS-style resource links — clients can discover available collections
        Map<String, String> resources = new LinkedHashMap<>();
        resources.put("rooms", "/api/v1/rooms");
        resources.put("sensors", "/api/v1/sensors");
        apiInfo.put("resources", resources);

        return Response.ok(apiInfo).build();
    }
}
