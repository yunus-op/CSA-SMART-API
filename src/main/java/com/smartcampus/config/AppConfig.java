package com.smartcampus.config;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * JAX-RS Application configuration class.
 *
 * The @ApplicationPath annotation sets the versioned base URI for all
 * REST resources to /api/v1.
 *
 * JAX-RS automatically discovers all @Path resource classes and @Provider
 * classes (ExceptionMappers, Filters) via package scanning configured in Main.
 */
@ApplicationPath("/api/v1")
public class AppConfig extends Application {
    // Resource discovery handled by ResourceConfig.packages() in Main.java
}
