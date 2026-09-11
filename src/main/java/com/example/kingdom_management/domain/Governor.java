package com.example.kingdom_management.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "governors")
public class Governor {

    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String governorName;

    private Long mainPower;

    // One Governor can have many Characters
    @OneToMany(mappedBy = "governor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Character> characters = new ArrayList<>();

    public Governor() {
    }

    public Governor(String governorName, Long mainPower) {
        this.governorName = governorName;
        this.mainPower = mainPower;
    }

    // Helper methods to manage bi-directional association
    public void addCharacter(Character character) {
        characters.add(character);
        character.setGovernor(this);
    }

    public void removeCharacter(Character character) {
        characters.remove(character);
        character.setGovernor(null);
    }

}