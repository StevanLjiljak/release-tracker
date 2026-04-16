package com.releasetracker.exception;

import java.util.UUID;

public class ReleaseNotFoundException extends RuntimeException {

    public ReleaseNotFoundException(UUID id) {
        super("Release not found with id: " + id);
    }
}
