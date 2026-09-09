package com.example.kingdom_management.domain;

import com.example.kingdom_management.domain.enums.CharacterType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "characters")
public class Character {

    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long characterId;

    @Column(nullable = false)
    private String characterName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CharacterType type;

    @OneToMany(mappedBy = "character", cascade = CascadeType.ALL)
    private List<AmeScore> scores = new ArrayList<>();

    private Long power;

    // Many Characters belong to one Governor
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "governor_id", nullable = false)
    private Governor governor;

    public Character() {
    }

    public Character(String characterName, CharacterType type, Long power) {
        this.characterName = characterName;
        this.type = type;
        this.power = power;
    }

}