package com.example.kingdom_management.web.form;

public class GovernorEditForm {

    private Long id;
    private String governorName;
    private Long mainPower;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGovernorName() {
        return governorName;
    }

    public void setGovernorName(String governorName) {
        this.governorName = governorName;
    }

    public Long getMainPower() {
        return mainPower;
    }

    public void setMainPower(Long mainPower) {
        this.mainPower = mainPower;
    }
}
