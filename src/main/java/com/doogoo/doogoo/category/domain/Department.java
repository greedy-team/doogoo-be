package com.doogoo.doogoo.category.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Department {

    @Id
    private Long id;
    @Column(nullable = false)
    private String departmentName;

    public Department(Long id, String departmentName) {
        this.departmentName = departmentName;
        this.id = id;
    }

}
