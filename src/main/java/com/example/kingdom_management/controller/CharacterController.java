package com.example.kingdom_management.controller;

import com.example.kingdom_management.domain.Character;
import com.example.kingdom_management.domain.Governor;
import com.example.kingdom_management.domain.enums.CharacterType;
import com.example.kingdom_management.service.CharacterService;
import com.example.kingdom_management.service.GovernorService;
import com.example.kingdom_management.web.form.CharacterEditForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/characters")
public class CharacterController {

    private final CharacterService characterService;
    private final GovernorService governorService;

    public CharacterController(CharacterService characterService, GovernorService governorService) {
        this.characterService = characterService;
        this.governorService = governorService;
    }

    @GetMapping("/import")
    public String showImportPage() {
        return "import";
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

    @GetMapping("/edit/{id}")
    public String editCharacter(@PathVariable Long id, Model model) {
        Character character = characterService.findById(id);
        model.addAttribute("form", toForm(character));
        addEditOptions(model);
        return "character-edit";
    }

    @PostMapping("/edit/{id}")
    public String updateCharacter(@PathVariable Long id,
                                  @ModelAttribute("form") CharacterEditForm form,
                                  Model model) {
        try {
            Character updated = characterService.update(id, form);
            return "redirect:/governors/" + updated.getGovernor().getId() + "?updated=character";
        } catch (IllegalArgumentException e) {
            form.setId(id);
            model.addAttribute("form", form);
            addEditOptions(model);
            model.addAttribute("error", e.getMessage());
            return "character-edit";
        }
    }

    private CharacterEditForm toForm(Character character) {
        CharacterEditForm form = new CharacterEditForm();
        form.setId(character.getId());
        form.setCharacterId(character.getCharacterId());
        form.setCharacterName(character.getCharacterName());
        form.setType(character.getType());
        form.setPower(character.getPower());
        form.setGovernorId(character.getGovernor().getId());
        return form;
    }

    private void addEditOptions(Model model) {
        model.addAttribute("governors", governorService.findAll());
        model.addAttribute("characterTypes", CharacterType.values());
    }
}
