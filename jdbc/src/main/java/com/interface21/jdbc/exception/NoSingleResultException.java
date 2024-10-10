package com.interface21.jdbc.exception;

public class NoSingleResultException extends RuntimeException{

    public NoSingleResultException(String message){
        super(message);
    }
}
