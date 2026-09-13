package com.example.kingdom_management.web.form;

import com.example.kingdom_management.domain.enums.CharacterType;

public class CharacterEditForm {

    private Long id;
    private Long characterId;
    private String characterName;
    private CharacterType type;
    private Long power;
    private Long governorId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCharacterId() {
        return characterId;
    }

    public void setCharacterId(Long characterId) {
        this.characterId = characterId;
    }

    public String getCharacterName() {
        return characterName;
    }

    public void setCharacterName(String characterName) {
        this.characterName = characterName;
    }

    public CharacterType getType() {
        return type;
    }

    public void setType(CharacterType type) {
        this.type = type;
    }

    public Long getPower() {
        return power;
    }

    public void setPower(Long power) {
        this.power = power;
    }

    public Long getGovernorId() {
        return governorId;
    }

    public void setGovernorId(Long governorId) {
        this.governorId = governorId;
    }
}
