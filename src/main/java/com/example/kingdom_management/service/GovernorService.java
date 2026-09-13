package com.example.kingdom_management.service;

import com.example.kingdom_management.domain.Governor;
import com.example.kingdom_management.repository.GovernorRepository;
import com.example.kingdom_management.web.form.GovernorEditForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GovernorService {

    private final GovernorRepository governorRepository;

    public GovernorService(GovernorRepository governorRepository) {
        this.governorRepository = governorRepository;
    }

    public List<Governor> findAll() {
        return governorRepository.findAll();
    }

    public Governor getGovernorById(Long id) {
        return governorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Governor not found: " + id));
    }

    public Governor findById(Long id) {
        return getGovernorById(id);
    }

    @Transactional
    public Governor update(Long id, GovernorEditForm form) {
        Governor governor = getGovernorById(id);
        String name = form.getGovernorName() == null ? "" : form.getGovernorName().trim();

        if (name.isBlank()) {
            throw new IllegalArgumentException("Governor name is required.");
        }
        if (form.getMainPower() != null && form.getMainPower() < 0) {
            throw new IllegalArgumentException("Main power cannot be negative.");
        }
        if (governorRepository.existsByGovernorNameAndIdNot(name, id)) {
            throw new IllegalArgumentException("A governor with that name already exists.");
        }

        governor.setGovernorName(name);
        governor.setMainPower(form.getMainPower());
        return governorRepository.save(governor);
    }
}
