package com.smartcampus.exception;

/**
 * Thrown when a client attempts to POST a new Sensor with a roomId
 * that does not reference any existing Room in the system.
 *
 * Mapped to HTTP 422 Unprocessable Entity by LinkedResourceNotFoundExceptionMapper.
 */
public class LinkedResourceNotFoundException extends RuntimeException {

    public LinkedResourceNotFoundException(String message) {
        super(message);
    }
}
