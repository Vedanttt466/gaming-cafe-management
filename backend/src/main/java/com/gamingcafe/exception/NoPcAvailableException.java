package com.gamingcafe.exception;

/** Thrown when all 10 PCs are occupied/reserved for the requested slot. */
public class NoPcAvailableException extends RuntimeException {
    public NoPcAvailableException(String message) {
        super(message);
    }
}
