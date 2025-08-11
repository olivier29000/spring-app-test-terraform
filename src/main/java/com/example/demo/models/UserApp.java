package com.example.demo.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.security.SecureRandom;
import java.util.Random;

@Entity
@Data
@NoArgsConstructor
@Table(name = "userapp")
public class UserApp {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(unique = true)
    private String username;
    private String password;
    private Role Role;
    private boolean enabled;
    private String tokenEnabled;

    public UserApp(String username, String password, Role role, boolean enabled) {
        this.username = username;
        this.password = password;
        Role = role;
        this.enabled = enabled;
        Integer n = 10;
        StringBuilder stringBuilder = new StringBuilder(n);
        String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random RANDOM = new SecureRandom();
        for (int i = 0; i < n; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            stringBuilder.append(CHARACTERS.charAt(index));
        }
        this.tokenEnabled = stringBuilder.toString();
    }
}
