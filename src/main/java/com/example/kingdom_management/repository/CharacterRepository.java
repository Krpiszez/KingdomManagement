package com.example.kingdom_management.repository;

import com.example.kingdom_management.domain.Character;
import com.example.kingdom_management.domain.enums.CharacterType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CharacterRepository extends JpaRepository<Character, Long> {

    Optional<Character> findByCharacterId(Long characterID);

    List<Character> findByType(CharacterType type);
}
