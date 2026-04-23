package com.smartcampus.exception;

/**
 * Thrown when a client attempts to POST a new reading to a Sensor
 * whose status is "MAINTENANCE" — meaning it is physically disconnected
 * and cannot accept new readings.
 *
 * Mapped to HTTP 403 Forbidden by SensorUnavailableExceptionMapper.
 */
public class SensorUnavailableException extends RuntimeException {

    public SensorUnavailableException(String message) {
        super(message);
    }
}
