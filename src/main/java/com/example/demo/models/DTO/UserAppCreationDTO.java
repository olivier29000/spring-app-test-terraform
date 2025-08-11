package com.example.demo.models.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserAppCreationDTO {
    private String username;
    private String password;
}