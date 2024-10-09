package com.interface21.dao;

public class ResultNotSingleException extends RuntimeException{

    public ResultNotSingleException(int resultSize) {
        super("Result Not Single - resultSize : " + resultSize);
    }
}
