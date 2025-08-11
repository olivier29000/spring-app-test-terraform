package com.example.demo.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Hello {

    @GetMapping("/hello")
    public String hello() {
        return "Hello World";
    }

    @GetMapping("/toto")
    public String toto() {
        System.out.println("toto");
        System.out.println("toto");
        System.out.println("toto");
        return "totoooooooooo";
    }
}
