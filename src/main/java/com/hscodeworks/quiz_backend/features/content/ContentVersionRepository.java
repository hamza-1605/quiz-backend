package com.hscodeworks.quiz_backend.features.content;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContentVersionRepository extends JpaRepository<ContentVersion, Short> {

    @Modifying
    @Query("UPDATE ContentVersion c SET c.version = c.version + 1 WHERE c.id = :id")
    int increment( @Param("id") Short id );
}