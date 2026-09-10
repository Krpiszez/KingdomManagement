package com.example.kingdom_management.domain.enums;

import java.util.Arrays;
import java.util.Comparator;

public enum AllianceRank {
    RANK_15(15, 200000),
    RANK_14(14, 175000),
    RANK_13(13, 160000),
    RANK_12(12, 150000),
    RANK_11(11, 125500),
    RANK_10(10, 115500),
    RANK_9(9, 92500),
    RANK_8(8, 82500),
    RANK_7(7, 60000),
    RANK_6(6, 40000),
    RANK_5(5, 25000),
    RANK_4(4, 15000),
    RANK_3(3, 7500),
    RANK_2(2, 3000),
    RANK_1(1, 1000);

    private final int level;
    private final int requiredScore;

    AllianceRank(int level, int requiredScore) {
        this.level = level;
        this.requiredScore = requiredScore;
    }

    public int getLevel() {
        return level;
    }

    public int getRequiredScore() {
        return requiredScore;
    }

    public static int getRankForScore(Integer score) {
        if (score == null) return 0;

        return Arrays.stream(values())
                .filter(rank -> score >= rank.getRequiredScore())
                .max(Comparator.comparingInt(AllianceRank::getRequiredScore))
                .map(AllianceRank::getLevel)
                .orElse(0);
    }
}