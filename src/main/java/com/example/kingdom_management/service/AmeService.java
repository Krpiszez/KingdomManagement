package com.example.kingdom_management.service;

import com.example.kingdom_management.domain.AmeScore;
import com.example.kingdom_management.domain.AmeWeek;
import com.example.kingdom_management.domain.Character;
import com.example.kingdom_management.domain.enums.AllianceRank;
import com.example.kingdom_management.domain.enums.CharacterType;
import com.example.kingdom_management.repository.AmeScoreRepository;
import com.example.kingdom_management.repository.AmeWeekRepository;
import jakarta.transaction.Transactional;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Service
public class AmeService {

    @Autowired
    @Lazy
    private CharacterService characterService;

    @Autowired
    private AmeScoreRepository ameScoreRepository;

    @Autowired
    private AmeWeekRepository ameWeekRepository;

    private static final List<String> REQUIRED_HEADERS = List.of("Player ID", "Score", "Tasks done", "Attempts used");

    @Transactional
    public void processAmeUpload(MultipartFile file, Integer weekNumber) {
        String weekName = "Week " + weekNumber;

        AmeWeek ameWeek = ameWeekRepository.findByName(weekName)
                .orElseGet(() -> {
                    AmeWeek newWeek = new AmeWeek();
                    newWeek.setName(weekName); // Adjust setter to match your entity property
                    return ameWeekRepository.save(newWeek);
                });

        int calculatedTotalScore = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreHeaderCase(true)
                     .setTrim(true)
                     .build())) {

            for (String requiredHeader : REQUIRED_HEADERS) {
                if (!csvParser.getHeaderNames().stream().anyMatch(h -> h.equalsIgnoreCase(requiredHeader))) {
                    throw new IllegalArgumentException("Missing required column: \"" + requiredHeader + "\"");
                }
            }

            for (CSVRecord record : csvParser) {
                Long characterId;
                Integer tasksDone;
                Integer attemptsUsed;
                Integer individualScore;

                try {
                    characterId = Long.valueOf(record.get("Player ID"));
                    tasksDone = Integer.valueOf(record.get("Tasks done"));
                    attemptsUsed = Integer.valueOf(record.get("Attempts used"));
                    individualScore = Integer.valueOf(record.get("Score"));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid numeric value on row " + record.getRecordNumber() + ": " + e.getMessage());
                }

                Character character = characterService.getCharacterById(characterId);

                String status = getAmeStatusForCharacter(character, individualScore);

                AmeScore ameScore = ameScoreRepository.findByCharacterIdAndAmeWeekId(character.getId(), ameWeek.getId())
                        .orElseGet(() -> {
                            AmeScore newScore = new AmeScore();
                            newScore.setCharacter(character);
                            newScore.setAmeWeek(ameWeek);
                            return ameScoreRepository.save(newScore);
                        });

                ameScore.setCharacter(character);
                ameScore.setAmeWeek(ameWeek);
                ameScore.setIndividualScore(individualScore);
                ameScore.setTasksDone(tasksDone);
                ameScore.setAttemptsUsed(attemptsUsed);
                ameScore.setStatus(status);

                ameScoreRepository.save(ameScore);
                calculatedTotalScore += individualScore;
            }

            ameWeek.setTotalScore(calculatedTotalScore);
            int rankLevel = AllianceRank.getRankForScore(calculatedTotalScore);
            ameWeek.setAmeRank(rankLevel);
            characterService.setTotalScoreForGovernors(ameWeek.getId());
            ameWeekRepository.save(ameWeek);
        } catch (Exception e) {
            throw new RuntimeException("Failed to process AME import file: " + e.getMessage(), e);
        }
    }

    private String getAmeStatusForCharacter(Character character, Integer individualScore) {
        if (character.getType().equals(CharacterType.MAIN)) {
            if (individualScore >= 2000) return "PASS";
            return "FAIL";
        } else if (character.getType().equals(CharacterType.FARM) || character.getType().equals(CharacterType.ALT)
                || character.getType().equals(CharacterType.FILLER)) {
            if (individualScore >= 1000) return "PASS";
            return "FAIL";
        } else {
            return "FAIL"; // Default case

        }
    }

    public List<AmeScore> getScoresByWeek(Long weekId) {
        if (weekId != null) {
            return ameScoreRepository.findByAmeWeekId(weekId);
        }
        return ameScoreRepository.findAll();
    }

    public List<AmeWeek> getAllWeeks() {
        return ameWeekRepository.findAll();
    }

    public List<AmeScore> getScoresByGovernorAndWeek(Long id, Long weekId) {
        return ameScoreRepository.findAllByCharacterIdAndAmeWeekId(id, weekId);
    }

    public List<AmeScore> getScoresWeekId(Long weekId) {
        return ameScoreRepository.findByAmeWeekId(weekId);
    }

    public List<AmeScore> getScoresCharacterId(Long characterId) {
        return ameScoreRepository.findByCharacterId(characterId);
    }

    public List<AmeScore> findByCharacterGovernorId(Long characterId) {
        return ameScoreRepository.findByCharacterGovernorId(characterId);
    }

    public List<AmeScore> getByCharacterGovernorIdAndAmeWeekId(Long characterId, Long weekId) {
        return ameScoreRepository.findByCharacterGovernorIdAndAmeWeekId(characterId, weekId);
    }

    public Optional<AmeScore> getScoreByCharacterIdAndAmeWeekId(Long characterId, Long weekId) {
        return ameScoreRepository.findByCharacterIdAndAmeWeekId(characterId, weekId);
    }

    public List<AmeScore> getScoreByGovernorIdAndAmeWeekId(Long governorId, Long weekId) {
        return ameScoreRepository.findByGovernorIdAndAmeWeekId(governorId, weekId);
    }

    public void saveAmeScore(AmeScore ameScore) {
        ameScoreRepository.save(ameScore);
    }
}