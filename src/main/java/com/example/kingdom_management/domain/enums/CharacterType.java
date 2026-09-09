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
}