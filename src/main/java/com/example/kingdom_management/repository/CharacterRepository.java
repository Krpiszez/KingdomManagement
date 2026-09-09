package com.example.kingdom_management.repository;

import com.example.kingdom_management.domain.Character;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CharacterRepository extends JpaRepository<Character, Long> {

    Optional<Character> findByCharacterId(Long characterID);
}
