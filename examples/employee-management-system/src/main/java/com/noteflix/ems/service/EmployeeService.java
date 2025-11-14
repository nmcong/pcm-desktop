package com.noteflix.ems.service;

import com.noteflix.ems.dto.EmployeeDTO;
import com.noteflix.ems.model.Employee;
import com.noteflix.ems.model.EmployeeStatus;

import java.util.List;

/**
 * Service interface for Employee operations
 */
public interface EmployeeService {
    
    /**
     * Get all employees
     */
    List<Employee> getAllEmployees();
    
    /**
     * Get all employees with department information
     */
    List<Employee> getAllEmployeesWithDepartment();
    
    /**
     * Get employee by ID
     */
    Employee getEmployeeById(Long id);
    
    /**
     * Get employee by ID with department information
     */
    Employee getEmployeeByIdWithDepartment(Long id);
    
    /**
     * Get employee by employee code
     */
    Employee getEmployeeByCode(String employeeCode);
    
    /**
     * Get employee by email
     */
    Employee getEmployeeByEmail(String email);
    
    /**
     * Get employees by department
     */
    List<Employee> getEmployeesByDepartment(Long departmentId);
    
    /**
     * Get employees by status
     */
    List<Employee> getEmployeesByStatus(EmployeeStatus status);
    
    /**
     * Search employees by keyword
     */
    List<Employee> searchEmployees(String keyword);
    
    /**
     * Search employees with department info
     */
    List<Employee> searchEmployeesWithDepartment(String keyword);
    
    /**
     * Create new employee from DTO
     */
    Employee createEmployee(EmployeeDTO employeeDTO);
    
    /**
     * Update existing employee from DTO
     */
    Employee updateEmployee(Long id, EmployeeDTO employeeDTO);
    
    /**
     * Delete employee
     */
    boolean deleteEmployee(Long id);
    
    /**
     * Check if employee exists
     */
    boolean existsById(Long id);
    
    /**
     * Check if employee code exists
     */
    boolean existsByCode(String employeeCode);
    
    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);
    
    /**
     * Count total employees
     */
    long countEmployees();
    
    /**
     * Count employees by status
     */
    long countEmployeesByStatus(EmployeeStatus status);
    
    /**
     * Count employees by department
     */
    long countEmployeesByDepartment(Long departmentId);
    
    /**
     * Convert Employee to EmployeeDTO
     */
    EmployeeDTO toDTO(Employee employee);
    
    /**
     * Convert EmployeeDTO to Employee
     */
    Employee toEntity(EmployeeDTO employeeDTO);
}

