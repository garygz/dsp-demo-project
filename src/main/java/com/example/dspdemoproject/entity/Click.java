package com.example.dspdemoproject.entity;

import jakarta.persistence.*;

import java.util.UUID;
@Entity
@Table(name = "clicks")
public class Click {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    private UUID impressionId;
}
