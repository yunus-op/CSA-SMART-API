package com.smartcampus;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * JAX-RS Application configuration class.
 * 
 * The @ApplicationPath annotation sets the base URI for all REST resources
 * to /api/v1, establishing a versioned API entry point.
 * 
 * JAX-RS will automatically discover and register all resource classes and
 * providers (ExceptionMappers, Filters) in the scanned packages.
 */
@ApplicationPath("/api/v1")
public class SmartCampusApplication extends Application {
    // JAX-RS will auto-discover resource classes via package scanning
    // configured in Main.java's ResourceConfig
}
