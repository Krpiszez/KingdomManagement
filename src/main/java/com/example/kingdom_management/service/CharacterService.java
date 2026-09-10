package com.example.kingdom_management.service;

import com.example.kingdom_management.domain.AmeScore;
import com.example.kingdom_management.domain.Character;
import com.example.kingdom_management.domain.Governor;
import com.example.kingdom_management.domain.enums.CharacterType;
import com.example.kingdom_management.repository.CharacterRepository;
import com.example.kingdom_management.repository.GovernorRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class CharacterService {

    private final GovernorRepository governorRepository;
    private final CharacterRepository characterRepository;
    private final AmeService ameService;

    public CharacterService(GovernorRepository governorRepository, CharacterRepository characterRepository, AmeService ameService) {
        this.governorRepository = governorRepository;
        this.characterRepository = characterRepository;
        this.ameService = ameService;
    }

    public void importData(MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename();

        if (filename != null && filename.toLowerCase().endsWith(".csv")) {
            processCsv(file);
        } else if (filename != null && (filename.toLowerCase().endsWith(".xlsx") || filename.toLowerCase().endsWith(".xls"))) {
            processExcel(file);
        } else {
            throw new IllegalArgumentException("Unsupported file type. Please upload a .csv, .xlsx, or .xls file.");
        }
    }

    private void processCsv(MultipartFile file) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreHeaderCase(true)
                     .setTrim(true)
                     .build())) {

            for (CSVRecord record : csvParser) {
                String governorName = record.isMapped("Governor Name") ? record.get("Governor Name") : "";
                String governorId = record.isMapped("Governor ID") ? record.get("Governor ID") : "";
                String ownerName = record.isMapped("Owner Name") ? record.get("Owner Name") : "";
                String status = record.isMapped("Status") ? record.get("Status") : "";

                String effectiveOwner = !ownerName.isBlank() ? ownerName : governorName;

                if (!effectiveOwner.isBlank() && !governorId.isBlank()) {
                    Governor gov = saveGovernorIfNotExists(effectiveOwner);
                    saveCharacterForGovernor(gov, governorId, governorName, status);
                }
            }
        }
    }

    private void processExcel(MultipartFile file) throws Exception {
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            Row headerRow = sheet.getRow(0);

            if (headerRow == null) return;

            int govNameIdx = -1, govIdIdx = -1, ownerNameIdx = -1, statusIdx = -1;

            for (Cell cell : headerRow) {
                String headerVal = formatter.formatCellValue(cell).trim();
                if (headerVal.equalsIgnoreCase("Governor Name")) govNameIdx = cell.getColumnIndex();
                else if (headerVal.equalsIgnoreCase("Governor ID")) govIdIdx = cell.getColumnIndex();
                else if (headerVal.equalsIgnoreCase("Owner Name")) ownerNameIdx = cell.getColumnIndex();
                else if (headerVal.equalsIgnoreCase("Status")) statusIdx = cell.getColumnIndex();
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String governorName = govNameIdx != -1 ? formatter.formatCellValue(row.getCell(govNameIdx)).trim() : "";
                String governorId = govIdIdx != -1 ? formatter.formatCellValue(row.getCell(govIdIdx)).trim() : "";
                String ownerName = ownerNameIdx != -1 ? formatter.formatCellValue(row.getCell(ownerNameIdx)).trim() : "";
                String status = statusIdx != -1 ? formatter.formatCellValue(row.getCell(statusIdx)).trim() : "";

                String effectiveOwner = !ownerName.isBlank() ? ownerName : governorName;

                if (!effectiveOwner.isBlank() && !governorId.isBlank()) {
                    Governor gov = saveGovernorIfNotExists(effectiveOwner);
                    saveCharacterForGovernor(gov, governorId, governorName, status);
                }
            }
        }
    }

    private void saveCharacterForGovernor(Governor gov, String governorIdStr, String governorName, String status) {
        if (gov == null || governorIdStr.isBlank() || governorName.isBlank()) {
            return;
        }

        try {
            Long parsedCharacterId = Long.parseLong(governorIdStr);

            if (characterRepository.findByCharacterId(parsedCharacterId).isEmpty()) {
                Character character = new Character();
                character.setCharacterId(parsedCharacterId);
                character.setCharacterName(governorName);
                character.setType(parseCharacterType(status));
                character.setGovernor(gov);

                characterRepository.save(character);
            }
        } catch (NumberFormatException e) {
            System.err.println("Skipping invalid character ID: " + governorIdStr);
        }
    }

    private CharacterType parseCharacterType(String status) {
        if (status == null || status.isBlank()) {
            return CharacterType.MAIN;
        }

        String cleanStatus = status.trim().toUpperCase().replaceAll("[^A-Z0-9_]", "_");

        try {
            return CharacterType.valueOf(cleanStatus);
        } catch (IllegalArgumentException e) {
            if (cleanStatus.contains("MAIN")) return CharacterType.MAIN;
            if (cleanStatus.contains("ALT")) return CharacterType.ALT;
            if (cleanStatus.contains("FARM")) return CharacterType.FARM;

            return CharacterType.MAIN;
        }
    }

    private Governor saveGovernorIfNotExists(String governorName) {
        if (governorName == null || governorName.isBlank()) {
            return null;
        }

        return governorRepository.findByGovernorName(governorName)
                .orElseGet(() -> {
                    Governor newGov = new Governor();
                    newGov.setGovernorName(governorName);
                    return governorRepository.save(newGov);
                });
    }

    public Character getCharacterById(Long characterId) {
        return characterRepository.findByCharacterId(characterId)
                .orElseThrow(() -> new RuntimeException("Character not found: " + characterId));
    }

    @Transactional
    public void setTotalScoreForGovernors(Long ameWeekId) {
        List<Character> mainCharacters = characterRepository.findByType(CharacterType.MAIN);

        for (Character mainCharacter : mainCharacters) {
            Governor governor = mainCharacter.getGovernor();
            if (governor == null) continue;

            int totalScore = governor.getCharacters().stream()
                    .filter(c -> c.getScores() != null)
                    .flatMap(c -> c.getScores().stream())
                    .filter(score -> score.getAmeWeek() != null && ameWeekId.equals(score.getAmeWeek().getId()))
                    .mapToInt(AmeScore::getIndividualScore)
                    .sum();

            // Fetch score safely using the updated repository method
            ameService.getScoreByCharacterIdAndAmeWeekId(mainCharacter.getId(), ameWeekId)
                    .ifPresent(ameScore -> {
                        ameScore.setTotalGovernorScore(totalScore);
                        ameService.saveAmeScore(ameScore);
                    });
        }
    }
}