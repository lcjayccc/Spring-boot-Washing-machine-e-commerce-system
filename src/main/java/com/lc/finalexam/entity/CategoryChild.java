package com.lc.finalexam.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "category_child")
@Data
public class CategoryChild {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private CategoryParent parent;

    @Column(nullable = false)
    private String name;

    private String description;
}
