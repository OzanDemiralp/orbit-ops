package com.ozandemiralp.orbit_tracker.exception;

public class TleServiceException extends RuntimeException {
    public TleServiceException(String message) {
        super(message);
    }

    public TleServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
