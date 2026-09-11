package com.example.kingdom_management.repository;

import com.example.kingdom_management.domain.AmeScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AmeScoreRepository extends JpaRepository<AmeScore, Long> {
    List<AmeScore> findAllByCharacterIdAndAmeWeekId(Long id, Long weekId);

    Optional<AmeScore> findByCharacterIdAndAmeWeekId(Long id, Long weekId);

    List<AmeScore> findByAmeWeekId(Long weekId);

    List<AmeScore> findByCharacterId(Long characterId);

    List<AmeScore> findByCharacterGovernorId(Long governorId);

    List<AmeScore> findByCharacterGovernorIdAndAmeWeekId(Long governorId, Long weekId);

    // AmeScoreRepository.java
    @Query("SELECT s FROM AmeScore s WHERE s.character.governor.id = :governorId AND s.ameWeek.id = :weekId")
    List<AmeScore> findByGovernorIdAndAmeWeekId(@Param("governorId") Long governorId, @Param("weekId") Long weekId);
}
