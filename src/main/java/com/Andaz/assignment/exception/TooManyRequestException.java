package com.Andaz.assignment.exception;

public class TooManyRequestException extends RuntimeException{

    public TooManyRequestException(String message){
        super(message);
    }
    
}
