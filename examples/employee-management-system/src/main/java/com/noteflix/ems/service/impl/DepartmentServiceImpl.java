package com.noteflix.ems.service.impl;

import com.noteflix.ems.mapper.DepartmentMapper;
import com.noteflix.ems.model.Department;
import com.noteflix.ems.service.DepartmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of DepartmentService
 */
@Service
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    private static final Logger logger = LoggerFactory.getLogger(DepartmentServiceImpl.class);

    private final DepartmentMapper departmentMapper;

    @Autowired
    public DepartmentServiceImpl(DepartmentMapper departmentMapper) {
        this.departmentMapper = departmentMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Department> getAllDepartments() {
        logger.debug("Getting all departments");
        return departmentMapper.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Department getDepartmentById(Long id) {
        logger.debug("Getting department by id: {}", id);
        return departmentMapper.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Department getDepartmentByName(String name) {
        logger.debug("Getting department by name: {}", name);
        return departmentMapper.findByName(name);
    }

    @Override
    public Department createDepartment(Department department) {
        logger.info("Creating new department: {}", department.getName());

        if (existsByName(department.getName())) {
            throw new IllegalArgumentException("Department with name '" + department.getName() + "' already exists");
        }

        departmentMapper.insert(department);
        logger.info("Department created with id: {}", department.getId());
        return department;
    }

    @Override
    public Department updateDepartment(Department department) {
        logger.info("Updating department with id: {}", department.getId());

        if (!existsById(department.getId())) {
            throw new IllegalArgumentException("Department with id " + department.getId() + " not found");
        }

        Department existing = departmentMapper.findByName(department.getName());
        if (existing != null && !existing.getId().equals(department.getId())) {
            throw new IllegalArgumentException("Department with name '" + department.getName() + "' already exists");
        }

        departmentMapper.update(department);
        logger.info("Department updated: {}", department.getId());
        return getDepartmentById(department.getId());
    }

    @Override
    public boolean deleteDepartment(Long id) {
        logger.info("Deleting department with id: {}", id);

        if (!existsById(id)) {
            throw new IllegalArgumentException("Department with id " + id + " not found");
        }

        int deleted = departmentMapper.deleteById(id);
        logger.info("Department deleted: {}", id);
        return deleted > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return departmentMapper.findById(id) != null;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return departmentMapper.findByName(name) != null;
    }

    @Override
    @Transactional(readOnly = true)
    public long countDepartments() {
        return departmentMapper.count();
    }
}

