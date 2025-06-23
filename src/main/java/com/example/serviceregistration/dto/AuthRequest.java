package com.example.serviceui.dto;

import lombok.Data;

@Data
public class AuthRequest {
    private String email;
    private String password;
}