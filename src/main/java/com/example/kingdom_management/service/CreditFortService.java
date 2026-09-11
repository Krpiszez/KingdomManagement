package com.example.kingdom_management.service;

import com.example.kingdom_management.domain.Character;
import com.example.kingdom_management.domain.CharacterCreditFortScore;
import com.example.kingdom_management.domain.CreditFortWeek;
import com.example.kingdom_management.domain.Governor;
import com.example.kingdom_management.domain.GovernorCreditFortSummary;
import com.example.kingdom_management.domain.enums.CharacterType;
import com.example.kingdom_management.repository.CharacterCreditFortScoreRepository;
import com.example.kingdom_management.repository.CreditFortWeekRepository;
import com.example.kingdom_management.repository.GovernorCreditFortSummaryRepository;
import com.example.kingdom_management.repository.GovernorRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Service
public class CreditFortService {

    @Autowired
    @Lazy
    private CharacterService characterService;

    @Autowired
    private GovernorRepository governorRepository;

    @Autowired
    private CreditFortWeekRepository creditFortWeekRepository;

    @Autowired
    private CharacterCreditFortScoreRepository characterCreditFortScoreRepository;

    @Autowired
    private GovernorCreditFortSummaryRepository governorCreditFortSummaryRepository;

    @Transactional
    public void processCreditFortUpload(MultipartFile file, Integer weekNumber) throws Exception {
        String weekName = "Week " + weekNumber;

        CreditFortWeek week = creditFortWeekRepository.findByName(weekName)
                .orElseGet(() -> {
                    CreditFortWeek newWeek = new CreditFortWeek();
                    newWeek.setWeekNumber(weekNumber);
                    newWeek.setName(weekName);
                    return creditFortWeekRepository.save(newWeek);
                });

        String filename = file.getOriginalFilename();

        if (filename != null && filename.toLowerCase().endsWith(".csv")) {
            processCsv(file, week);
        } else if (filename != null && (filename.toLowerCase().endsWith(".xlsx") || filename.toLowerCase().endsWith(".xls"))) {
            processExcel(file, week);
        } else {
            throw new IllegalArgumentException("Unsupported file type. Please upload a .csv, .xlsx, or .xls file.");
        }

        recomputeGovernorSummaries(week);
    }

    private void processCsv(MultipartFile file, CreditFortWeek week) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreHeaderCase(true)
                     .setTrim(true)
                     .build())) {

            for (CSVRecord record : csvParser) {
                String characterIdStr = firstMapped(record, "Player ID", "Character ID", "ID");
                String buildStr = firstMapped(record, "Building Time (seconds)", "Build Credits", "Build", "Build Donations");
                String techStr = firstMapped(record, "Mad Scientist Points", "Tech Credits", "Tech", "Tech Donations");
                String fortsStr = firstMapped(record, "Fort Destroyed", "Forts Done", "Forts", "Forts Completed");

                applyRow(week, characterIdStr, buildStr, techStr, fortsStr);
            }
        }
    }

    private String firstMapped(CSVRecord record, String... headerNames) {
        for (String header : headerNames) {
            if (record.isMapped(header)) {
                return record.get(header);
            }
        }
        return "";
    }

    private void processExcel(MultipartFile file, CreditFortWeek week) throws Exception {
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            Row headerRow = sheet.getRow(0);

            if (headerRow == null) return;

            int idIdx = -1, buildIdx = -1, techIdx = -1, fortsIdx = -1;

            for (Cell cell : headerRow) {
                String headerVal = formatter.formatCellValue(cell).trim();
                if (headerVal.equalsIgnoreCase("Player ID") || headerVal.equalsIgnoreCase("Character ID") || headerVal.equalsIgnoreCase("ID")) {
                    idIdx = cell.getColumnIndex();
                } else if (headerVal.equalsIgnoreCase("Building Time (seconds)") || headerVal.equalsIgnoreCase("Build Credits") || headerVal.equalsIgnoreCase("Build") || headerVal.equalsIgnoreCase("Build Donations")) {
                    buildIdx = cell.getColumnIndex();
                } else if (headerVal.equalsIgnoreCase("Mad Scientist Points") || headerVal.equalsIgnoreCase("Tech Credits") || headerVal.equalsIgnoreCase("Tech") || headerVal.equalsIgnoreCase("Tech Donations")) {
                    techIdx = cell.getColumnIndex();
                } else if (headerVal.equalsIgnoreCase("Fort Destroyed") || headerVal.equalsIgnoreCase("Forts Done") || headerVal.equalsIgnoreCase("Forts") || headerVal.equalsIgnoreCase("Forts Completed")) {
                    fortsIdx = cell.getColumnIndex();
                }
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String characterIdStr = idIdx != -1 ? formatter.formatCellValue(row.getCell(idIdx)).trim() : "";
                String buildStr = buildIdx != -1 ? formatter.formatCellValue(row.getCell(buildIdx)).trim() : "";
                String techStr = techIdx != -1 ? formatter.formatCellValue(row.getCell(techIdx)).trim() : "";
                String fortsStr = fortsIdx != -1 ? formatter.formatCellValue(row.getCell(fortsIdx)).trim() : "";

                applyRow(week, characterIdStr, buildStr, techStr, fortsStr);
            }
        }
    }

    private void applyRow(CreditFortWeek week, String characterIdStr, String buildStr, String techStr, String fortsStr) {
        if (characterIdStr == null || characterIdStr.isBlank()) {
            return;
        }

        Long characterId;
        try {
            characterId = Long.valueOf(characterIdStr.trim());
        } catch (NumberFormatException e) {
            System.err.println("Skipping row with invalid character ID: " + characterIdStr);
            return;
        }

        Character character = characterService.getCharacterById(characterId);

        int buildCredits = parseIntOrZero(buildStr);
        int techCredits = parseIntOrZero(techStr);
        int fortsDone = parseIntOrZero(fortsStr);

        CharacterCreditFortScore score = characterCreditFortScoreRepository
                .findByCharacterIdAndCreditFortWeekId(character.getId(), week.getId())
                .orElseGet(() -> {
                    CharacterCreditFortScore newScore = new CharacterCreditFortScore();
                    newScore.setCharacter(character);
                    newScore.setCreditFortWeek(week);
                    return characterCreditFortScoreRepository.save(newScore);
                });

        score.setBuildCredits(buildCredits);
        score.setTechCredits(techCredits);
        score.setFortsDone(fortsDone);

        characterCreditFortScoreRepository.save(score);
    }

    private int parseIntOrZero(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        try {
            // Strip thousands separators like "100,000" just in case.
            return Integer.parseInt(value.trim().replace(",", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Transactional
    public void recomputeGovernorSummaries(CreditFortWeek week) {
        int fortRequirement = week.getFortRequirement() != null ? week.getFortRequirement() : 70;
        List<Governor> governors = governorRepository.findAll();

        for (Governor governor : governors) {
            if (governor.getCharacters() == null || governor.getCharacters().isEmpty()) {
                continue;
            }

            List<CharacterCreditFortScore> scores = characterCreditFortScoreRepository
                    .findByCharacterGovernorIdAndCreditFortWeekId(governor.getId(), week.getId());

            int totalCredits = scores.stream()
                    .mapToInt(CharacterCreditFortScore::getTotalCredits)
                    .sum();

            int requiredCredits = governor.getCharacters().stream()
                    .mapToInt(c -> c.getType().getRequiredCredits())
                    .sum();

            int totalForts = scores.stream()
                    .filter(s -> s.getCharacter() != null && s.getCharacter().getType().countsTowardFortRequirement())
                    .mapToInt(s -> s.getFortsDone() == null ? 0 : s.getFortsDone())
                    .sum();

            int mainForts = scores.stream()
                    .filter(s -> s.getCharacter() != null && s.getCharacter().getType() == CharacterType.MAIN)
                    .mapToInt(s -> s.getFortsDone() == null ? 0 : s.getFortsDone())
                    .sum();

            GovernorCreditFortSummary summary = governorCreditFortSummaryRepository
                    .findByGovernorIdAndCreditFortWeekId(governor.getId(), week.getId())
                    .orElseGet(() -> {
                        GovernorCreditFortSummary newSummary = new GovernorCreditFortSummary();
                        newSummary.setGovernor(governor);
                        newSummary.setCreditFortWeek(week);
                        return newSummary;
                    });

            summary.setTotalCredits(totalCredits);
            summary.setRequiredCredits(requiredCredits);
            summary.setCreditStatus(totalCredits >= requiredCredits ? "PASS" : "FAIL");
            summary.setTotalForts(totalForts);
            summary.setFortRequirement(fortRequirement);
            summary.setFortStatus(totalForts >= fortRequirement ? "PASS" : "FAIL");
            summary.setMainForts(mainForts);

            governorCreditFortSummaryRepository.save(summary);
        }
    }

    public List<CreditFortWeek> getAllWeeks() {
        return creditFortWeekRepository.findAll();
    }

    public List<CharacterCreditFortScore> getScoresByWeek(Long weekId) {
        if (weekId != null) {
            return characterCreditFortScoreRepository.findByCreditFortWeekId(weekId);
        }
        return characterCreditFortScoreRepository.findAll();
    }

    public List<GovernorCreditFortSummary> getSummariesByWeek(Long weekId) {
        if (weekId != null) {
            return governorCreditFortSummaryRepository.findByCreditFortWeekId(weekId);
        }
        return governorCreditFortSummaryRepository.findAll();
    }

    public Optional<GovernorCreditFortSummary> getSummaryForGovernorAndWeek(Long governorId, Long weekId) {
        return governorCreditFortSummaryRepository.findByGovernorIdAndCreditFortWeekId(governorId, weekId);
    }

    public List<CharacterCreditFortScore> getScoresForGovernorAndWeek(Long governorId, Long weekId) {
        return characterCreditFortScoreRepository.findByCharacterGovernorIdAndCreditFortWeekId(governorId, weekId);
    }
}
