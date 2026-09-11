package com.example.kingdom_management.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "character_credit_fort_scores")
public class CharacterCreditFortScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer buildCredits = 0;
    private Integer techCredits = 0;
    private Integer fortsDone = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id")
    private Character character;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "week_id")
    private CreditFortWeek creditFortWeek;

    public int getTotalCredits() {
        return (buildCredits == null ? 0 : buildCredits) + (techCredits == null ? 0 : techCredits);
    }

    // Getters and Setters
}
