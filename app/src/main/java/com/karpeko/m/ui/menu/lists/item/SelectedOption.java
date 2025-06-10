package com.karpeko.m.ui.menu.lists.item;

public class SelectedOption {
    private String groupName;
    private String selectedValue;

    public SelectedOption() {}

    public SelectedOption(String groupName, String selectedValue) {
        this.groupName = groupName;
        this.selectedValue = selectedValue;
    }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public String getSelectedValue() { return selectedValue; }
    public void setSelectedValue(String selectedValue) { this.selectedValue = selectedValue; }
}

