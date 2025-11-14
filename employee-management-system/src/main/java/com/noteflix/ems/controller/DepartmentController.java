package com.noteflix.ems.controller;

import com.noteflix.ems.model.Department;
import com.noteflix.ems.service.DepartmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for Department management
 */
@Controller
@RequestMapping("/departments")
public class DepartmentController {
    
    private static final Logger logger = LoggerFactory.getLogger(DepartmentController.class);
    
    private final DepartmentService departmentService;
    
    @Autowired
    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }
    
    /**
     * List all departments
     */
    @GetMapping
    public String listDepartments(Model model) {
        List<Department> departments = departmentService.getAllDepartments();
        model.addAttribute("departments", departments);
        return "departments/list";
    }
    
    /**
     * AJAX endpoint to get all departments
     */
    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<List<Department>> getAllDepartments() {
        try {
            List<Department> departments = departmentService.getAllDepartments();
            return ResponseEntity.ok(departments);
        } catch (Exception e) {
            logger.error("Error getting departments via API", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * AJAX endpoint to get department by ID
     */
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Department> getDepartment(@PathVariable Long id) {
        try {
            Department department = departmentService.getDepartmentById(id);
            if (department == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(department);
        } catch (Exception e) {
            logger.error("Error getting department via API", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}

