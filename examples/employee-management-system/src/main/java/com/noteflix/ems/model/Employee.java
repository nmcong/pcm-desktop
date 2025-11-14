package com.noteflix.ems.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Employee entity representing an employee in the system
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Employee {
    private Long id;
    private String employeeCode;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private LocalDate hireDate;
    private BigDecimal salary;
    private Long departmentId;
    private String position;
    private EmployeeStatus status;
    private String address;
    private String city;
    private String country;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Transient field for department info
    private Department department;
    
    /**
     * Get full name of employee
     */
    public String getFullName() {
        return lastName + " " + firstName;
    }
}

