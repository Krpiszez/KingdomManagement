package com.example.kingdom_management.controller;

import com.example.kingdom_management.domain.AmeScore;
import com.example.kingdom_management.domain.AmeWeek;
import com.example.kingdom_management.domain.Character;
import com.example.kingdom_management.domain.Governor;
import com.example.kingdom_management.domain.CharacterCreditFortScore;
import com.example.kingdom_management.domain.CreditFortWeek;
import com.example.kingdom_management.domain.GovernorCreditFortSummary;
import com.example.kingdom_management.repository.GovernorRepository; // Adjust repository name
import com.example.kingdom_management.service.AmeService;
import com.example.kingdom_management.service.CreditFortService;
import com.example.kingdom_management.service.GovernorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/governors")
public class GovernorController {

    @Autowired
    private GovernorService governorService; // Ensure repository is injected

    @Autowired
    private AmeService ameService;

    @Autowired
    private CreditFortService creditFortService;

    @GetMapping("")
    public String governorManagement(Model model) {
        // Fetch all governors and pass them to the Thymeleaf model
        model.addAttribute("governors", governorService.findAll());
        return "governors";
    }

    @GetMapping("/{id}")
    public String getGovernorDetail(@PathVariable("id") Long id,
                                    @RequestParam(name = "weekId", required = false) Long weekId,
                                    @RequestParam(name = "creditFortWeekId", required = false) Long creditFortWeekId,
                                    Model model) {

        Governor governor = governorService.findById(id);
        model.addAttribute("governor", governor);

        // Pass characters and available weeks
        model.addAttribute("characters", governor.getCharacters());
        model.addAttribute("weeks", ameService.getAllWeeks());

        // Pass the currently selected week back to Thymeleaf for th:selected
        model.addAttribute("selectedWeekId", weekId);

        // Fetch scores ONLY if a weekId is actually selected
        List<AmeScore> ameScores = Collections.emptyList();
        if (weekId != null) {
            ameScores = ameService.getByCharacterGovernorIdAndAmeWeekId(id, weekId);
        }
        model.addAttribute("ameScores", ameScores);

        // Credit & Fort tracking - uses its own week list/selection since it
        // runs on a separate weekly cycle from AME.
        List<CreditFortWeek> creditFortWeeks = creditFortService.getAllWeeks();
        model.addAttribute("creditFortWeeks", creditFortWeeks);

        CreditFortWeek selectedCreditFortWeek = null;
        if (creditFortWeekId != null) {
            selectedCreditFortWeek = creditFortWeeks.stream()
                    .filter(w -> w.getId().equals(creditFortWeekId))
                    .findFirst()
                    .orElse(null);
        } else if (!creditFortWeeks.isEmpty()) {
            selectedCreditFortWeek = creditFortWeeks.getLast(); // latest week
        }
        model.addAttribute("selectedCreditFortWeekId", selectedCreditFortWeek != null ? selectedCreditFortWeek.getId() : null);

        Optional<GovernorCreditFortSummary> creditFortSummary = Optional.empty();
        List<CharacterCreditFortScore> creditFortScores = Collections.emptyList();
        if (selectedCreditFortWeek != null) {
            creditFortSummary = creditFortService.getSummaryForGovernorAndWeek(id, selectedCreditFortWeek.getId());
            creditFortScores = creditFortService.getScoresForGovernorAndWeek(id, selectedCreditFortWeek.getId());
        }
        model.addAttribute("creditFortSummary", creditFortSummary.orElse(null));
        model.addAttribute("creditFortScores", creditFortScores);

        return "governor-detail";
    }
}
