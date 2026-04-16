package com.example.dspdemoproject.controller.exceptions;

public class InvalidInputDataProvided extends RuntimeException{
    public InvalidInputDataProvided(String errMsg){
        super(errMsg);
    }
}
