package com.smartcampus.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * JAX-RS Filter for cross-cutting API observability (logging).
 *
 * Implements BOTH ContainerRequestFilter and ContainerResponseFilter in one class
 * to capture the full request-response lifecycle.
 *
 * - Request filter: logs the HTTP method and URI of every incoming request.
 * - Response filter: logs the final HTTP status code of every outgoing response.
 *
 * The @Provider annotation registers this filter with the JAX-RS runtime automatically.
 * It applies globally to ALL resource methods without any code changes to resources.
 *
 * Design advantage: Using a filter for cross-cutting concerns (logging, auth, CORS)
 * keeps resource classes clean and focused on business logic. Adding a manual
 * Logger.info() to every resource method would violate DRY, scatter logging logic,
 * and make future changes (e.g., changing log format) require editing every class.
 */
@Provider
public class ApiLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOGGER = Logger.getLogger(ApiLoggingFilter.class.getName());

    /**
     * Executed before the request reaches the resource method.
     * Logs the HTTP method and full request URI.
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        LOGGER.info(String.format(
                "[REQUEST]  --> %s %s",
                requestContext.getMethod(),
                requestContext.getUriInfo().getRequestUri()
        ));
    }

    /**
     * Executed after the resource method has produced a response.
     * Logs the final HTTP status code sent back to the client.
     */
    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        LOGGER.info(String.format(
                "[RESPONSE] <-- %d %s | %s %s",
                responseContext.getStatus(),
                responseContext.getStatusInfo().getReasonPhrase(),
                requestContext.getMethod(),
                requestContext.getUriInfo().getRequestUri()
        ));
    }
}
