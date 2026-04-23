package com.smartcampus.exceptions;

public class LinkedResourceNotFoundException extends RuntimeException {
    private final String missingId;

    public LinkedResourceNotFoundException(String missingId) {
        super("Referenced resource not found: roomId '" + missingId + "' does not exist.");
        this.missingId = missingId;
    }

    public String getMissingId() { return missingId; }
}
