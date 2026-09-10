package com.example.kingdom_management.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "ame_weeks")
public class AmeWeek {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Integer weekNumber; // e.g., 1 for "1st Week", 2 for "2nd Week"
    private LocalDate startDate;
    private LocalDate endDate;

    @OneToMany(mappedBy = "ameWeek", cascade = CascadeType.ALL)
    private List<AmeScore> scores = new ArrayList<>();

    // Getters and Setters
}
