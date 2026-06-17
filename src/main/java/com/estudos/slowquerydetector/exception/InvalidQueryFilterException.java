package com.estudos.slowquerydetector.exception;

public class InvalidQueryFilterException extends RuntimeException {

    public InvalidQueryFilterException(String message) {
        super(message);
    }
}
