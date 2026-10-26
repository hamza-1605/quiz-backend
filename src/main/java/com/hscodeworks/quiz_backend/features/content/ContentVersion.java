package com.hscodeworks.quiz_backend.features.content;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.Getter;

@Entity
@Getter
@Table(name = "content_version")
@NoArgsConstructor( access = AccessLevel.PROTECTED )
public class ContentVersion {

    @Id
    private Short id;

    @Column(nullable = false)
    private Long version;
}