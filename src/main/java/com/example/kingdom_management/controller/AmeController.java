package com.example.kingdom_management.controller;

import com.example.kingdom_management.domain.AmeScore;
import com.example.kingdom_management.domain.AmeWeek;
import com.example.kingdom_management.service.AmeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

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

    @GetMapping("/scores/calculate")
    public String getScoreCalculations(@RequestParam(value = "weekId", required = false) Long weekId, Model model) {
        List<AmeWeek> weeks = ameService.getAllWeeks();
        model.addAttribute("weeks", weeks);
        model.addAttribute("selectedWeekId", weekId);
        model.addAttribute("calculatedScores", ameService.getScoresByWeek(weekId));

        // Find selected week or default to the latest uploaded week
        AmeWeek selectedWeek = null;
        if (weekId != null) {
            selectedWeek = weeks.stream()
                    .filter(w -> w.getId().equals(weekId))
                    .findFirst()
                    .orElse(null);
        } else if (!weeks.isEmpty()) {
            selectedWeek = weeks.getLast(); // latest week
        }

        model.addAttribute("selectedWeek", selectedWeek);

        return "ame-score-calculation";
    }
}