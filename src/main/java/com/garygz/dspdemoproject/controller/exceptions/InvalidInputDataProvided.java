package com.garygz.dspdemoproject.controller.exceptions;

public class InvalidInputDataProvided extends RuntimeException{
    public InvalidInputDataProvided(String errMsg){
        super(errMsg);
    }
}
