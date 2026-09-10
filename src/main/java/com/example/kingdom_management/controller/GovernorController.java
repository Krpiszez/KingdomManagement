package com.example.kingdom_management.controller;

import com.example.kingdom_management.repository.GovernorRepository; // Adjust repository name
import com.example.kingdom_management.service.GovernorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GovernorController {

    @Autowired
    private GovernorService governorService; // Ensure repository is injected

    @GetMapping("/governors")
    public String governorManagement(Model model) {
        // Fetch all governors and pass them to the Thymeleaf model
        model.addAttribute("governors", governorService.findAll());
        return "governors";
    }
}
