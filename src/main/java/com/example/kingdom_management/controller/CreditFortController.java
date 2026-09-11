package com.example.kingdom_management.controller;

import com.example.kingdom_management.domain.CreditFortWeek;
import com.example.kingdom_management.service.CreditFortService;
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
@RequestMapping("/credit-fort")
public class CreditFortController {

    @Autowired
    private CreditFortService creditFortService;

    @GetMapping("/import")
    public String showImportForm() {
        return "credit-fort-import";
    }

    @PostMapping("/import")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                    @RequestParam("weekNumber") Integer weekNumber,
                                    RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select a file to upload.");
            return "redirect:/credit-fort/import";
        }

        try {
            creditFortService.processCreditFortUpload(file, weekNumber);
            redirectAttributes.addFlashAttribute("message", "Credit & fort records uploaded successfully for Week " + weekNumber + "!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error processing upload: " + e.getMessage());
        }

        return "redirect:/credit-fort/import";
    }

    @GetMapping("/tracker")
    public String getTracker(@RequestParam(value = "weekId", required = false) Long weekId, Model model) {
        List<CreditFortWeek> weeks = creditFortService.getAllWeeks();
        model.addAttribute("weeks", weeks);

        CreditFortWeek selectedWeek = null;
        if (weekId != null) {
            selectedWeek = weeks.stream()
                    .filter(w -> w.getId().equals(weekId))
                    .findFirst()
                    .orElse(null);
        } else if (!weeks.isEmpty()) {
            selectedWeek = weeks.getLast(); // latest week
        }

        model.addAttribute("selectedWeek", selectedWeek);
        model.addAttribute("selectedWeekId", selectedWeek != null ? selectedWeek.getId() : null);
        model.addAttribute("summaries", creditFortService.getSummariesByWeek(selectedWeek != null ? selectedWeek.getId() : null));

        return "credit-fort-tracker";
    }
}
