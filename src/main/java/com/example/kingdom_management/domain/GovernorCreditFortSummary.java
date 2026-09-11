package com.example.kingdom_management.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Per-governor, per-week rollup of the alliance credit (build + tech) and
 * fort requirements. Recomputed from CharacterCreditFortScore rows every
 * time a weekly import runs, so it can be persisted for fast lookups
 * without having to re-sum the whole roster on every page load.
 */
@Getter
@Setter
@Entity
@Table(name = "governor_credit_fort_summaries")
public class GovernorCreditFortSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "governor_id")
    private Governor governor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "week_id")
    private CreditFortWeek creditFortWeek;

    // Credits: sum of (buildCredits + techCredits) across every character
    // the governor owns, vs the sum of each character type's requirement.
    private Integer totalCredits = 0;
    private Integer requiredCredits = 0;
    private String creditStatus; // PASS / FAIL

    // Forts: sum of fortsDone across MAIN + FARM characters only, vs the
    // week's flat fort requirement (default 70).
    private Integer totalForts = 0;
    private Integer fortRequirement = 70;
    private String fortStatus; // PASS / FAIL

    // Forts done by MAIN characters specifically, tracked separately for
    // reward purposes - NOT part of the pass/fail requirement above.
    private Integer mainForts = 0;

    // Getters and Setters
}
