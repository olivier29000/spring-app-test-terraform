package com.example.demo.controllers;

import com.example.demo.services.S3Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final S3Service s3Service;

    public FileController(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    @GetMapping("/upload-url")
    public ResponseEntity<String> getUploadUrl(@RequestParam String filename, @RequestParam String contentType) {
        String key = UUID.randomUUID() + "-" + filename;
        String url = s3Service.generatePresignedPutUrl(key, contentType);
        return ResponseEntity.ok(url);
    }

    @GetMapping("/download-url")
    public ResponseEntity<String> getDownloadUrl(@RequestParam String key) {
        String url = s3Service.generatePresignedGetUrl(key);
        return ResponseEntity.ok(url);
    }
}

