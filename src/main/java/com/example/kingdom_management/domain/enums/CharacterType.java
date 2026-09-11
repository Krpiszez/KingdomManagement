package com.example.kingdom_management.domain.enums;

public enum CharacterType {
    MAIN,
    FARM,
    ALT,
    DEADWEIGHT,
    FILLER;

    public static CharacterType fromString(String text) {
        if (text == null || text.isBlank()) {
            return MAIN; // Default fallback
        }

        for (CharacterType type : CharacterType.values()) {
            if (type.name().equalsIgnoreCase(text.trim())) {
                return type;
            }
        }

        throw new IllegalArgumentException("Unknown character type: " + text);
    }

    /**
     * Weekly alliance-credit requirement (build + tech donations combined)
     * for this character type. Summed across a governor's whole roster to
     * get that governor's total required credits for the week.
     */
    public int getRequiredCredits() {
        return switch (this) {
            case MAIN -> 100_000;
            case FARM, ALT, FILLER -> 120_000;
            case DEADWEIGHT -> 0;
        };
    }

    /**
     * Whether forts done on this character count toward the governor's
     * combined 70-fort weekly requirement. Fort counts are still recorded
     * for every character type, this only controls what counts toward the
     * pass/fail requirement.
     */
    public boolean countsTowardFortRequirement() {
        return this == MAIN || this == FARM || this == ALT || this == FILLER;
    }
}