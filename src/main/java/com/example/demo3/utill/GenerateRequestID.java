package com.example.demo3.utill;

import java.util.UUID;

public class GenerateRequestID {
    public static String generateRequestID() {
        return UUID.randomUUID().toString();
    }
}
