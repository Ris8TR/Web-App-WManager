package com.myTesi.aloisioUmberto.exception;

public class ManyRequestException extends RuntimeException {
    //It's called when a bucket has exceeded his limit
    public ManyRequestException(String message) {
        super(message);
    }
}
