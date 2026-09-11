package com.example.kingdom_management.repository;

import com.example.kingdom_management.domain.GovernorCreditFortSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GovernorCreditFortSummaryRepository extends JpaRepository<GovernorCreditFortSummary, Long> {

    Optional<GovernorCreditFortSummary> findByGovernorIdAndCreditFortWeekId(Long governorId, Long weekId);

    List<GovernorCreditFortSummary> findByCreditFortWeekId(Long weekId);
}
