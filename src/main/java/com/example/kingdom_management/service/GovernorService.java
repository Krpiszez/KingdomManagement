package com.example.kingdom_management.service;

import com.example.kingdom_management.controller.GovernorController;
import com.example.kingdom_management.domain.Governor;
import com.example.kingdom_management.repository.GovernorRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GovernorService {

    @Autowired
    private GovernorRepository governorRepository;

    public List<Governor> findAll() {
        return governorRepository.findAll();
    }
}
