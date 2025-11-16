package com.noteflix.ems.mapper;

import com.noteflix.ems.model.Department;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * MyBatis Mapper interface for Department operations
 */
@Mapper
public interface DepartmentMapper {

    /**
     * Find all departments
     */
    List<Department> findAll();

    /**
     * Find department by ID
     */
    Department findById(Long id);

    /**
     * Find department by name
     */
    Department findByName(String name);

    /**
     * Insert new department
     */
    int insert(Department department);

    /**
     * Update existing department
     */
    int update(Department department);

    /**
     * Delete department by ID
     */
    int deleteById(Long id);

    /**
     * Count total departments
     */
    long count();
}

