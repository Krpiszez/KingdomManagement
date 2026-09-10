package com.example.kingdom_management.repository;

import com.example.kingdom_management.domain.AmeWeek;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AmeWeekRepository extends JpaRepository<AmeWeek, Long> {
    Optional<AmeWeek> findByName(String weekName);


}
