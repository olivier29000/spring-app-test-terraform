package com.example.demo.models;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class TokenModifMotDePasse {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private UserApp userApp;
    private String token;
    private LocalDateTime localDateTime;

    public TokenModifMotDePasse(UserApp userApp, String token, LocalDateTime localDateTime) {
        this.userApp = userApp;
        this.token = token;
        this.localDateTime = localDateTime;
    }
}