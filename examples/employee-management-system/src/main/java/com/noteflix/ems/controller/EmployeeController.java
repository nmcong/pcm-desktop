package com.noteflix.ems.controller;

import com.noteflix.ems.dto.EmployeeDTO;
import com.noteflix.ems.model.Employee;
import com.noteflix.ems.model.EmployeeStatus;
import com.noteflix.ems.service.DepartmentService;
import com.noteflix.ems.service.EmployeeService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for Employee management
 */
@Controller
@RequestMapping("/employees")
public class EmployeeController {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);

    private final EmployeeService employeeService;
    private final DepartmentService departmentService;

    @Autowired
    public EmployeeController(EmployeeService employeeService, DepartmentService departmentService) {
        this.employeeService = employeeService;
        this.departmentService = departmentService;
    }

    /**
     * List all employees
     */
    @GetMapping
    public String listEmployees(@RequestParam(required = false) String search, Model model) {
        List<Employee> employees;

        if (search != null && !search.trim().isEmpty()) {
            employees = employeeService.searchEmployeesWithDepartment(search);
            model.addAttribute("search", search);
        } else {
            employees = employeeService.getAllEmployeesWithDepartment();
        }

        model.addAttribute("employees", employees);
        model.addAttribute("departments", departmentService.getAllDepartments());
        return "employees/list";
    }

    /**
     * Show form to create new employee
     */
    @GetMapping("/new")
    public String newEmployeeForm(Model model) {
        model.addAttribute("employeeDTO", new EmployeeDTO());
        model.addAttribute("departments", departmentService.getAllDepartments());
        model.addAttribute("statuses", EmployeeStatus.values());
        return "employees/form";
    }

    /**
     * Show form to edit employee
     */
    @GetMapping("/edit/{id}")
    public String editEmployeeForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Employee employee = employeeService.getEmployeeById(id);
            if (employee == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy nhân viên với ID: " + id);
                return "redirect:/employees";
            }

            EmployeeDTO employeeDTO = employeeService.toDTO(employee);
            model.addAttribute("employeeDTO", employeeDTO);
            model.addAttribute("departments", departmentService.getAllDepartments());
            model.addAttribute("statuses", EmployeeStatus.values());
            model.addAttribute("isEdit", true);
            return "employees/form";
        } catch (Exception e) {
            logger.error("Error loading employee for edit", e);
            redirectAttributes.addFlashAttribute("error", "Lỗi khi tải thông tin nhân viên: " + e.getMessage());
            return "redirect:/employees";
        }
    }

    /**
     * View employee details
     */
    @GetMapping("/view/{id}")
    public String viewEmployee(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Employee employee = employeeService.getEmployeeByIdWithDepartment(id);
            if (employee == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy nhân viên với ID: " + id);
                return "redirect:/employees";
            }

            model.addAttribute("employee", employee);
            return "employees/view";
        } catch (Exception e) {
            logger.error("Error loading employee details", e);
            redirectAttributes.addFlashAttribute("error", "Lỗi khi tải thông tin nhân viên: " + e.getMessage());
            return "redirect:/employees";
        }
    }

    /**
     * Create new employee
     */
    @PostMapping
    public String createEmployee(@Valid @ModelAttribute EmployeeDTO employeeDTO,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("departments", departmentService.getAllDepartments());
            model.addAttribute("statuses", EmployeeStatus.values());
            return "employees/form";
        }

        try {
            Employee employee = employeeService.createEmployee(employeeDTO);
            redirectAttributes.addFlashAttribute("success", "Tạo nhân viên thành công: " + employee.getFullName());
            return "redirect:/employees";
        } catch (IllegalArgumentException e) {
            logger.warn("Validation error while creating employee", e);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("departments", departmentService.getAllDepartments());
            model.addAttribute("statuses", EmployeeStatus.values());
            return "employees/form";
        } catch (Exception e) {
            logger.error("Error creating employee", e);
            model.addAttribute("error", "Lỗi khi tạo nhân viên: " + e.getMessage());
            model.addAttribute("departments", departmentService.getAllDepartments());
            model.addAttribute("statuses", EmployeeStatus.values());
            return "employees/form";
        }
    }

    /**
     * Update employee
     */
    @PostMapping("/update/{id}")
    public String updateEmployee(@PathVariable Long id,
                                 @Valid @ModelAttribute EmployeeDTO employeeDTO,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("departments", departmentService.getAllDepartments());
            model.addAttribute("statuses", EmployeeStatus.values());
            model.addAttribute("isEdit", true);
            return "employees/form";
        }

        try {
            Employee employee = employeeService.updateEmployee(id, employeeDTO);
            redirectAttributes.addFlashAttribute("success", "Cập nhật nhân viên thành công: " + employee.getFullName());
            return "redirect:/employees";
        } catch (IllegalArgumentException e) {
            logger.warn("Validation error while updating employee", e);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("departments", departmentService.getAllDepartments());
            model.addAttribute("statuses", EmployeeStatus.values());
            model.addAttribute("isEdit", true);
            return "employees/form";
        } catch (Exception e) {
            logger.error("Error updating employee", e);
            model.addAttribute("error", "Lỗi khi cập nhật nhân viên: " + e.getMessage());
            model.addAttribute("departments", departmentService.getAllDepartments());
            model.addAttribute("statuses", EmployeeStatus.values());
            model.addAttribute("isEdit", true);
            return "employees/form";
        }
    }

    /**
     * Delete employee
     */
    @PostMapping("/delete/{id}")
    public String deleteEmployee(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Employee employee = employeeService.getEmployeeById(id);
            if (employee == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy nhân viên với ID: " + id);
                return "redirect:/employees";
            }

            String employeeName = employee.getFullName();
            employeeService.deleteEmployee(id);
            redirectAttributes.addFlashAttribute("success", "Đã xóa nhân viên: " + employeeName);
        } catch (Exception e) {
            logger.error("Error deleting employee", e);
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa nhân viên: " + e.getMessage());
        }

        return "redirect:/employees";
    }

    /**
     * AJAX endpoint to get employee by ID
     */
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<EmployeeDTO> getEmployee(@PathVariable Long id) {
        try {
            Employee employee = employeeService.getEmployeeByIdWithDepartment(id);
            if (employee == null) {
                return ResponseEntity.notFound().build();
            }

            EmployeeDTO dto = employeeService.toDTO(employee);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            logger.error("Error getting employee via API", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * AJAX endpoint to search employees
     */
    @GetMapping("/api/search")
    @ResponseBody
    public ResponseEntity<List<EmployeeDTO>> searchEmployees(@RequestParam String keyword) {
        try {
            List<Employee> employees = employeeService.searchEmployeesWithDepartment(keyword);
            List<EmployeeDTO> dtos = employees.stream()
                    .map(employeeService::toDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            logger.error("Error searching employees via API", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * AJAX endpoint to check if employee code exists
     */
    @GetMapping("/api/check-code")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> checkEmployeeCode(
            @RequestParam String code,
            @RequestParam(required = false) Long excludeId) {
        try {
            Employee employee = employeeService.getEmployeeByCode(code);
            boolean exists = employee != null && (excludeId == null || !employee.getId().equals(excludeId));

            Map<String, Boolean> response = new HashMap<>();
            response.put("exists", exists);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error checking employee code", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * AJAX endpoint to check if email exists
     */
    @GetMapping("/api/check-email")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> checkEmail(
            @RequestParam String email,
            @RequestParam(required = false) Long excludeId) {
        try {
            Employee employee = employeeService.getEmployeeByEmail(email);
            boolean exists = employee != null && (excludeId == null || !employee.getId().equals(excludeId));

            Map<String, Boolean> response = new HashMap<>();
            response.put("exists", exists);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error checking email", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

