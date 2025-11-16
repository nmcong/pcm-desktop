package com.noteflix.ems.model;

/**
 * Employee status enum
 */
public enum EmployeeStatus {
    ACTIVE("Đang làm việc"),
    INACTIVE("Nghỉ việc"),
    ON_LEAVE("Đang nghỉ phép"),
    SUSPENDED("Tạm ngưng");

    private final String displayName;

    EmployeeStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

