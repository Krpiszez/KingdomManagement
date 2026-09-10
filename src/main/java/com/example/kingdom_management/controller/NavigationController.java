package com.example.kingdom_management.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NavigationController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/performance-tracker")
    public String performanceTracker() {
        return "performance-tracker";
    }

    @GetMapping("/score-calculation")
    public String scoreCalculation() {
        return "score-calculation";
    }

//    @GetMapping("/ame/import")
//    public String ameImport() {
//        return "ame-import";
//    }
}