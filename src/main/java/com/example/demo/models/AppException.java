package com.example.demo.models;

public class AppException extends RuntimeException {
    public AppException(String message) {
        super(message);
    }
}