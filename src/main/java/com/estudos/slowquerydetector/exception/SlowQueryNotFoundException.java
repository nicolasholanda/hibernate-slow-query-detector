package com.estudos.slowquerydetector.exception;

import java.util.UUID;

public class SlowQueryNotFoundException extends RuntimeException {

    public SlowQueryNotFoundException(UUID id) {
        super("Slow query record not found: " + id);
    }
}
