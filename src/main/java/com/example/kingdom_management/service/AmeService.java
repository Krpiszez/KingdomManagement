package com.example.kingdom_management.service;

import com.example.kingdom_management.domain.AmeScore;
import com.example.kingdom_management.domain.AmeWeek;
import com.example.kingdom_management.domain.Character;
import com.example.kingdom_management.repository.AmeScoreRepository;
import com.example.kingdom_management.repository.AmeWeekRepository;
import com.example.kingdom_management.repository.CharacterRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Service
public class AmeService {

    @Autowired
    private CharacterRepository characterRepository;

    @Autowired
    private AmeScoreRepository ameScoreRepository;

    @Autowired
    private AmeWeekRepository ameWeekRepository;

    @Transactional
    public void processAmeUpload(MultipartFile file, Integer weekNumber) {
        String weekName = "Week " + weekNumber;

        AmeWeek ameWeek = ameWeekRepository.findByName(weekName)
                .orElseGet(() -> {
                    AmeWeek newWeek = new AmeWeek();
                    newWeek.setName(weekName); // Adjust setter to match your entity property
                    return ameWeekRepository.save(newWeek);
                });

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            int characterNameIdx = -1;
            int characterIdIdx = -1;
            int ameScoreIdx = -1;
            int ameTaskDoneIdx = -1;
            int ameAttemptUsedIdx = -1;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {

                String[] values = line.split(",");

                if (isHeader) {
                    for (int i = 0; i < values.length; i++) {
                        String headerName = values[i].replaceAll("^[^a-zA-Z0-9]+", "").trim();
                        if (headerName.equalsIgnoreCase("Name")) {
                            characterNameIdx = i;
                        } else if (headerName.equalsIgnoreCase("Player ID")) {
                            characterIdIdx = i;
                        } else if (headerName.equalsIgnoreCase("Score")) {
                            ameScoreIdx = i;
                        } else if (headerName.equalsIgnoreCase("Tasks done")) {
                            ameTaskDoneIdx = i;
                        } else if (headerName.equalsIgnoreCase("Attempts used")) {
                            ameAttemptUsedIdx = i;
                        }
                    }
                    isHeader = false;
                    continue;
                }

                String characterName = values[characterNameIdx];
                Long characterId = Long.valueOf(values[characterIdIdx]);
                Integer tasksDone = Integer.valueOf(values[ameTaskDoneIdx]);
                Integer attempsUsed = Integer.valueOf(values[ameAttemptUsedIdx]);
                Integer individualScore = Integer.valueOf(values[ameScoreIdx]);



                Character character = characterRepository.findByCharacterId(characterId)
                        .orElseThrow(() -> new RuntimeException("Character not found: " + characterId));

                AmeScore ameScore = new AmeScore();
                ameScore.setCharacter(character);
                ameScore.setAmeWeek(ameWeek);
                ameScore.setIndividualScore(individualScore);
                ameScore.setTasksDone(tasksDone);
                ameScore.setAttemptsUsed(attempsUsed);

                ameScoreRepository.save(ameScore);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to process AME import file: " + e.getMessage(), e);
        }
    }
}