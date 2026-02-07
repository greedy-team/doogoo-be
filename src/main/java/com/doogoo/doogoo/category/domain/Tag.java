package com.doogoo.doogoo.category.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Tag {

    @Id
    private Long id;

    @Column(nullable = false)
    private String tagName;

    public Tag(Long id, String tagName) {
        this.tagName = tagName;
        this.id = id;
    }


}
