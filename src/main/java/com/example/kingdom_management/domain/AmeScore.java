package com.example.kingdom_management.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "ame_scores")
public class AmeScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer individualScore;
    private Integer totalGovernorScore;
    private Integer tasksDone;
    private Integer attemptsUsed;
    private String status; // PASS / FAIL

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id")
    private Character character;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "week_id")
    private AmeWeek ameWeek;

    // Getters and Setters
}
