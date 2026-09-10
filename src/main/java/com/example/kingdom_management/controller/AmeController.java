package com.example.kingdom_management.controller;

import com.example.kingdom_management.service.AmeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/ame")
public class AmeController {

    @Autowired
    private AmeService ameService;

    @GetMapping("/import")
    public String showImportForm() {
        return "ame-import";
    }

    @PostMapping("/import")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   @RequestParam("weekNumber") Integer weekNumber,
                                   RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select a file to upload.");
            return "redirect:/ame/import";
        }

        try {
            ameService.processAmeUpload(file, weekNumber);
            redirectAttributes.addFlashAttribute("message", "AME records uploaded successfully for Week " + weekNumber + "!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error processing upload: " + e.getMessage());
        }

        return "redirect:/ame/import";
    }
}