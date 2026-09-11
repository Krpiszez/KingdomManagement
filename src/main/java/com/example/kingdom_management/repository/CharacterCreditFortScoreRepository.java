package com.example.kingdom_management.repository;

import com.example.kingdom_management.domain.CharacterCreditFortScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CharacterCreditFortScoreRepository extends JpaRepository<CharacterCreditFortScore, Long> {

    Optional<CharacterCreditFortScore> findByCharacterIdAndCreditFortWeekId(Long characterId, Long weekId);

    List<CharacterCreditFortScore> findByCreditFortWeekId(Long weekId);

    List<CharacterCreditFortScore> findByCharacterId(Long characterId);

    List<CharacterCreditFortScore> findByCharacterGovernorIdAndCreditFortWeekId(Long governorId, Long weekId);
}
