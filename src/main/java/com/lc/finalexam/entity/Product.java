package com.lc.finalexam.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "product")
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    private String model;
    private Double price;
    private String motorType;
    private String capacity;
    private String imageUrl;
    private String description;

    @ManyToMany
    @JoinTable(
            name = "product_category_mapping",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_child_id")
    )
    private List<CategoryChild> categories;
}
