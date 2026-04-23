/**
 * JAX-RS filter for logging all incoming requests
 * and outgoing responses.
 */


package com.smartcampus.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

@Provider
public class LoggingFilter implements ContainerRequestFilter,
        ContainerResponseFilter {

    private static final Logger LOGGER =
            Logger.getLogger(LoggingFilter.class.getName());

    // Logs every incoming request
    @Override
    public void filter(ContainerRequestContext requestContext)
            throws IOException {
        LOGGER.info("Incoming Request: ["
                + requestContext.getMethod()
                + "] "
                + requestContext.getUriInfo().getRequestUri());
    }

    // Logs every outgoing response
    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext)
            throws IOException {
        LOGGER.info("Outgoing Response: ["
                + requestContext.getMethod()
                + "] "
                + requestContext.getUriInfo().getRequestUri()
                + " -> Status: "
                + responseContext.getStatus());
    }
}