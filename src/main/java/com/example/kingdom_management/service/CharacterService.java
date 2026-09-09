package com.example.kingdom_management.service;

import com.example.kingdom_management.domain.Character;
import com.example.kingdom_management.domain.Governor;
import com.example.kingdom_management.domain.enums.CharacterType;
import com.example.kingdom_management.repository.CharacterRepository;
import com.example.kingdom_management.repository.GovernorRepository;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Service
public class CharacterService {

    private final GovernorRepository governorRepository;
    private final CharacterRepository characterRepository;

    public CharacterService(GovernorRepository governorRepository, CharacterRepository characterRepository) {
        this.governorRepository = governorRepository;
        this.characterRepository = characterRepository;
    }

    public void importData(MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename();

        if (filename != null && filename.endsWith(".csv")) {
            processCsv(file);
        } else if (filename != null && (filename.endsWith(".xlsx") || filename.endsWith(".xls"))) {
            processExcel(file);
        } else {
            throw new IllegalArgumentException("Unsupported file type. Please upload a .csv, .xlsx, or .xls file.");
        }
    }

    private void processCsv(MultipartFile file) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;

            int govNameIdx = -1;
            int govIdIdx = -1;
            int ownerNameIdx = -1;
            int statusIdx = -1;

            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }

                String[] values = line.split(",");

                if (isHeader) {
                    for (int i = 0; i < values.length; i++) {
                        String headerName = values[i].trim();
                        if (headerName.equalsIgnoreCase("Governor Name")) {
                            govNameIdx = i;
                        } else if (headerName.equalsIgnoreCase("Governor ID")) {
                            govIdIdx = i;
                        } else if (headerName.equalsIgnoreCase("Owner Name")) {
                            ownerNameIdx = i;
                        } else if (headerName.equalsIgnoreCase("Status")) {
                            statusIdx = i;
                        }
                    }
                    isHeader = false;
                    continue;
                }

                String governorName = (govNameIdx != -1 && govNameIdx < values.length) ? values[govNameIdx].trim() : "";
                String governorId = (govIdIdx != -1 && govIdIdx < values.length) ? values[govIdIdx].trim() : "";
                String ownerName = (ownerNameIdx != -1 && ownerNameIdx < values.length) ? values[ownerNameIdx].trim() : "";
                String status = (statusIdx != -1 && statusIdx < values.length) ? values[statusIdx].trim() : "";

                // Determine owner name: Fallback to governorName if ownerName column is blank
                String effectiveOwner = !ownerName.isEmpty() ? ownerName : governorName;

                if (!effectiveOwner.isEmpty() && !governorId.isEmpty()) {
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

            // Check if character already exists by ID
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
            return CharacterType.MAIN; // Default fallback
        }

        // Clean string (e.g. "Main Account" -> "MAIN_ACCOUNT" or "MAIN")
        String cleanStatus = status.trim().toUpperCase().replaceAll("[^A-Z0-9_]", "_");

        try {
            return CharacterType.valueOf(cleanStatus);
        } catch (IllegalArgumentException e) {
            // Match against partial names or fall back to standard types
            if (cleanStatus.contains("MAIN")) return CharacterType.MAIN;
            if (cleanStatus.contains("ALT")) return CharacterType.ALT;
            if (cleanStatus.contains("FARM")) return CharacterType.FARM;

            return CharacterType.MAIN; // Fallback
        }
    }

    private void processExcel(MultipartFile file) throws Exception {
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Cell governorNameCell = row.getCell(0);
                if (governorNameCell != null) {
                    String governorName = governorNameCell.getStringCellValue().trim();
                    saveGovernorIfNotExists(governorName);
                }
            }
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
}