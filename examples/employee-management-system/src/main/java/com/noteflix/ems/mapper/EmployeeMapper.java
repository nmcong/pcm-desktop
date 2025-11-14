package com.noteflix.ems.mapper;

import com.noteflix.ems.model.Employee;
import com.noteflix.ems.model.EmployeeStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * MyBatis Mapper interface for Employee operations
 */
@Mapper
public interface EmployeeMapper {
    
    /**
     * Find all employees
     */
    List<Employee> findAll();
    
    /**
     * Find all employees with department information
     */
    List<Employee> findAllWithDepartment();
    
    /**
     * Find employee by ID
     */
    Employee findById(Long id);
    
    /**
     * Find employee by ID with department information
     */
    Employee findByIdWithDepartment(Long id);
    
    /**
     * Find employee by employee code
     */
    Employee findByEmployeeCode(String employeeCode);
    
    /**
     * Find employee by email
     */
    Employee findByEmail(String email);
    
    /**
     * Find employees by department
     */
    List<Employee> findByDepartmentId(Long departmentId);
    
    /**
     * Find employees by status
     */
    List<Employee> findByStatus(EmployeeStatus status);
    
    /**
     * Search employees by keyword (name, email, code)
     */
    List<Employee> search(@Param("keyword") String keyword);
    
    /**
     * Search employees with department info
     */
    List<Employee> searchWithDepartment(@Param("keyword") String keyword);
    
    /**
     * Insert new employee
     */
    int insert(Employee employee);
    
    /**
     * Update existing employee
     */
    int update(Employee employee);
    
    /**
     * Delete employee by ID
     */
    int deleteById(Long id);
    
    /**
     * Count total employees
     */
    long count();
    
    /**
     * Count employees by status
     */
    long countByStatus(EmployeeStatus status);
    
    /**
     * Count employees by department
     */
    long countByDepartment(Long departmentId);
}

