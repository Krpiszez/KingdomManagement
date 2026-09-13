package com.example.kingdom_management.repository;

import com.example.kingdom_management.domain.Governor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GovernorRepository extends JpaRepository<Governor, Long> {

    Optional<Governor> findByGovernorNameAndCharactersCharacterName(String governorName, String characterName);

    Optional<Governor> findByGovernorName(String name);

    boolean existsByGovernorNameAndIdNot(String governorName, Long id);
}
