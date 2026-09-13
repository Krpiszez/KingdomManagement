package com.example.kingdom_management.controller;

import com.example.kingdom_management.domain.AmeScore;
import com.example.kingdom_management.domain.Character;
import com.example.kingdom_management.domain.CharacterCreditFortScore;
import com.example.kingdom_management.domain.CreditFortWeek;
import com.example.kingdom_management.domain.Governor;
import com.example.kingdom_management.domain.GovernorCreditFortSummary;
import com.example.kingdom_management.service.AmeService;
import com.example.kingdom_management.service.CreditFortService;
import com.example.kingdom_management.service.GovernorService;
import com.example.kingdom_management.web.form.GovernorEditForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/governors")
public class GovernorController {

    private final GovernorService governorService;
    private final AmeService ameService;
    private final CreditFortService creditFortService;

    public GovernorController(GovernorService governorService,
                              AmeService ameService,
                              CreditFortService creditFortService) {
        this.governorService = governorService;
        this.ameService = ameService;
        this.creditFortService = creditFortService;
    }

    @GetMapping("")
    public String governorManagement(Model model) {
        model.addAttribute("governors", governorService.findAll());
        return "governors";
    }

    @GetMapping("/edit/{id}")
    public String editGovernor(@PathVariable Long id, Model model) {
        Governor governor = governorService.findById(id);

        GovernorEditForm form = new GovernorEditForm();
        form.setId(governor.getId());
        form.setGovernorName(governor.getGovernorName());
        form.setMainPower(governor.getMainPower());

        model.addAttribute("form", form);
        model.addAttribute("governor", governor);
        return "governor-edit";
    }

    @PostMapping("/edit/{id}")
    public String updateGovernor(@PathVariable Long id,
                                  @ModelAttribute("form") GovernorEditForm form,
                                  Model model) {
        try {
            governorService.update(id, form);
            return "redirect:/governors/" + id + "?updated=governor";
        } catch (IllegalArgumentException e) {
            form.setId(id);
            model.addAttribute("form", form);
            model.addAttribute("governor", governorService.findById(id));
            model.addAttribute("error", e.getMessage());
            return "governor-edit";
        }
    }

    @GetMapping("/{id}")
    public String getGovernorDetail(@PathVariable("id") Long id,
                                    @RequestParam(name = "weekId", required = false) Long weekId,
                                    @RequestParam(name = "creditFortWeekId", required = false) Long creditFortWeekId,
                                    @RequestParam(name = "updated", required = false) String updated,
                                    Model model) {

        Governor governor = governorService.findById(id);
        model.addAttribute("governor", governor);
        model.addAttribute("characters", governor.getCharacters());
        model.addAttribute("weeks", ameService.getAllWeeks());
        model.addAttribute("selectedWeekId", weekId);

        List<AmeScore> ameScores = Collections.emptyList();
        if (weekId != null) {
            ameScores = ameService.getByCharacterGovernorIdAndAmeWeekId(id, weekId);
        }
        model.addAttribute("ameScores", ameScores);

        List<CreditFortWeek> creditFortWeeks = creditFortService.getAllWeeks();
        model.addAttribute("creditFortWeeks", creditFortWeeks);

        CreditFortWeek selectedCreditFortWeek = null;
        if (creditFortWeekId != null) {
            selectedCreditFortWeek = creditFortWeeks.stream()
                    .filter(w -> w.getId().equals(creditFortWeekId))
                    .findFirst()
                    .orElse(null);
        } else if (!creditFortWeeks.isEmpty()) {
            selectedCreditFortWeek = creditFortWeeks.getLast();
        }
        model.addAttribute("selectedCreditFortWeekId",
                selectedCreditFortWeek != null ? selectedCreditFortWeek.getId() : null);

        Optional<GovernorCreditFortSummary> creditFortSummary = Optional.empty();
        List<CharacterCreditFortScore> creditFortScores = Collections.emptyList();
        if (selectedCreditFortWeek != null) {
            creditFortSummary = creditFortService.getSummaryForGovernorAndWeek(id, selectedCreditFortWeek.getId());
            creditFortScores = creditFortService.getScoresForGovernorAndWeek(id, selectedCreditFortWeek.getId());
        }
        model.addAttribute("creditFortSummary", creditFortSummary.orElse(null));
        model.addAttribute("creditFortScores", creditFortScores);

        if ("governor".equals(updated)) {
            model.addAttribute("message", "Governor updated successfully.");
        } else if ("character".equals(updated)) {
            model.addAttribute("message", "Character updated successfully.");
        }

        return "governor-detail";
    }
}
