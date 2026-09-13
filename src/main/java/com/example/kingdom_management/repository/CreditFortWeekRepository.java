package com.example.kingdom_management.repository;

import com.example.kingdom_management.domain.CreditFortWeek;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CreditFortWeekRepository extends JpaRepository<CreditFortWeek, Long> {

    Optional<CreditFortWeek> findByName(String name);

    List<CreditFortWeek> findAllByOrderByWeekNumberAsc();
}
