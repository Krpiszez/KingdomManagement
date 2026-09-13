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
@Table(name = "credit_fort_weeks")
public class CreditFortWeek {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Integer weekNumber; // e.g., 1 for "1st Week", 2 for "2nd Week"
    private LocalDate startDate;
    private LocalDate endDate;

    // Flat combined-fort requirement per governor (MAIN + FARM forts only).
    // Kept per-week (rather than a hardcoded constant) in case the alliance
    // changes the requirement in future weeks.
    private Integer fortRequirement = 70;
    private Long totalFortsDone;
    private Long totalBuildingScore;
    private Long totalTechScore;

    /**
     * Combined weekly credit score (build + tech).
     * Kept as a derived value so there is only one source of truth for the
     * persisted build and tech totals.
     */
    @Transient
    public Long getTotalCreditScore() {
        return (totalBuildingScore == null ? 0L : totalBuildingScore)
                + (totalTechScore == null ? 0L : totalTechScore);
    }


    @OneToMany(mappedBy = "creditFortWeek", cascade = CascadeType.ALL)
    private List<CharacterCreditFortScore> scores = new ArrayList<>();

    // Getters and Setters
}
