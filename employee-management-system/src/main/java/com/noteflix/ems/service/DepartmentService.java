package com.noteflix.ems.service;

import com.noteflix.ems.model.Department;

import java.util.List;

/**
 * Service interface for Department operations
 */
public interface DepartmentService {
    
    /**
     * Get all departments
     */
    List<Department> getAllDepartments();
    
    /**
     * Get department by ID
     */
    Department getDepartmentById(Long id);
    
    /**
     * Get department by name
     */
    Department getDepartmentByName(String name);
    
    /**
     * Create new department
     */
    Department createDepartment(Department department);
    
    /**
     * Update existing department
     */
    Department updateDepartment(Department department);
    
    /**
     * Delete department
     */
    boolean deleteDepartment(Long id);
    
    /**
     * Check if department exists
     */
    boolean existsById(Long id);
    
    /**
     * Check if department name exists
     */
    boolean existsByName(String name);
    
    /**
     * Count total departments
     */
    long countDepartments();
}

