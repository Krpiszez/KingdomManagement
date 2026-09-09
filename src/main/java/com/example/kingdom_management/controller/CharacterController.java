package com.example.kingdom_management.controller;

import com.example.kingdom_management.service.CharacterService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/characters")
public class CharacterController {

    private final CharacterService characterService;

    public CharacterController(CharacterService characterService) {
        this.characterService = characterService;
    }

    @GetMapping("/import")
    public String showImportPage() {
        return "import"; // Renders src/main/resources/templates/import.html
    }

    @PostMapping("/import")
    public String handleImport(@RequestParam("file") MultipartFile file, Model model) {
        if (file.isEmpty()) {
            model.addAttribute("error", "Please select a file to upload.");
            return "import";
        }

        try {
            characterService.importData(file);
            model.addAttribute("message", "File processed and imported successfully!");
        } catch (Exception e) {
            model.addAttribute("error", "Failed to process file: " + e.getMessage());
        }

        return "import";
    }
}