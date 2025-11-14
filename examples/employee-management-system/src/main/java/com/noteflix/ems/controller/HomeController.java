package com.noteflix.ems.controller;

import com.noteflix.ems.model.EmployeeStatus;
import com.noteflix.ems.service.DepartmentService;
import com.noteflix.ems.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Home controller for the application
 */
@Controller
public class HomeController {
    
    private final EmployeeService employeeService;
    private final DepartmentService departmentService;
    
    @Autowired
    public HomeController(EmployeeService employeeService, DepartmentService departmentService) {
        this.employeeService = employeeService;
        this.departmentService = departmentService;
    }
    
    @GetMapping("/")
    public String home(Model model) {
        // Get statistics for dashboard
        long totalEmployees = employeeService.countEmployees();
        long activeEmployees = employeeService.countEmployeesByStatus(EmployeeStatus.ACTIVE);
        long totalDepartments = departmentService.countDepartments();
        
        model.addAttribute("totalEmployees", totalEmployees);
        model.addAttribute("activeEmployees", activeEmployees);
        model.addAttribute("totalDepartments", totalDepartments);
        
        return "index";
    }
}

