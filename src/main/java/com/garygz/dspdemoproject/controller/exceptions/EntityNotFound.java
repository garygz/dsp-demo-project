package com.garygz.dspdemoproject.controller.exceptions;

public class EntityNotFound extends RuntimeException{
    public EntityNotFound(String msg) {
        super(msg);
    }
}
