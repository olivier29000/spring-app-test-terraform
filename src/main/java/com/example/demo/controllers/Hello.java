package com.example.demo.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Hello {

    @GetMapping("/back/hello")
    public String hello() {
        return "Hello World";
    }

    @GetMapping("/hello")
    public String dsvvds() {
        return "Hello test";
    }


    @GetMapping("/toto")
    public String toto() {
        System.out.println("toto");
        System.out.println("toto");
        System.out.println("toto");
        return "totoooooooooo";
    }
}
