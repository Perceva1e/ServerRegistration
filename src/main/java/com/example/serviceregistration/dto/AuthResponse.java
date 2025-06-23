package com.example.serviceui.dto;

import lombok.Data;

@Data
public class AuthResponse {
    private Long id;
    private String email;
    private String name;
    private boolean success;
    private String message;
}