package com.hscodeworks.quiz_backend.features.category;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "category")
@Getter
@NoArgsConstructor( access = AccessLevel.PROTECTED )
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Setter 
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Setter
    @Column(length = 20)
    private String color;
    
    @Setter
    @Column(name = "image_url", length = 500)
    private String imageUrl;
    
    @Setter
    @Column(name = "image_public_id", length = 255)
    private String imagePublicId;


    public Category(String name, String color) {
        this.name = name;
        this.color = color;
    }
}