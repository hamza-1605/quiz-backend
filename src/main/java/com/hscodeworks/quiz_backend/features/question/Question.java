package com.hscodeworks.quiz_backend.features.question;

import java.util.ArrayList;
import java.util.List;

import com.hscodeworks.quiz_backend.features.category.Category;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "question")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;


    @Setter
    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;


    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuestionType type;


    @Setter
    @Column(columnDefinition = "TEXT")
    private String explanation;


    @Setter
    @Column(name = "image_url", length = 500)
    private String imageUrl;


    @Setter
    @Column(name = "image_public_id", length = 255)
    private String imagePublicId;


    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<QuestionOption> options = new ArrayList<>();


    public void replaceOptions( List<QuestionOption> newOptions ) {
        options.clear();
        newOptions.forEach(this::addOption);
    }


    private void addOption( QuestionOption option ) {
        option.setQuestion(this);
        options.add(option);
    }
}