package com.example.demo.models.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailSupportDTO {
    private Integer id;
    private String title;
    private String content;
    private Integer echelle;
    private String imageBase64;
    private String contactPreference;
    private String contact;
}
