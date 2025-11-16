package com.noteflix.ems.service.impl;

import com.noteflix.ems.dto.EmployeeDTO;
import com.noteflix.ems.mapper.EmployeeMapper;
import com.noteflix.ems.model.Employee;
import com.noteflix.ems.model.EmployeeStatus;
import com.noteflix.ems.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of EmployeeService
 */
@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    private final EmployeeMapper employeeMapper;

    @Autowired
    public EmployeeServiceImpl(EmployeeMapper employeeMapper) {
        this.employeeMapper = employeeMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Employee> getAllEmployees() {
        logger.debug("Getting all employees");
        return employeeMapper.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Employee> getAllEmployeesWithDepartment() {
        logger.debug("Getting all employees with department");
        return employeeMapper.findAllWithDepartment();
    }

    @Override
    @Transactional(readOnly = true)
    public Employee getEmployeeById(Long id) {
        logger.debug("Getting employee by id: {}", id);
        return employeeMapper.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Employee getEmployeeByIdWithDepartment(Long id) {
        logger.debug("Getting employee by id with department: {}", id);
        return employeeMapper.findByIdWithDepartment(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Employee getEmployeeByCode(String employeeCode) {
        logger.debug("Getting employee by code: {}", employeeCode);
        return employeeMapper.findByEmployeeCode(employeeCode);
    }

    @Override
    @Transactional(readOnly = true)
    public Employee getEmployeeByEmail(String email) {
        logger.debug("Getting employee by email: {}", email);
        return employeeMapper.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Employee> getEmployeesByDepartment(Long departmentId) {
        logger.debug("Getting employees by department: {}", departmentId);
        return employeeMapper.findByDepartmentId(departmentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Employee> getEmployeesByStatus(EmployeeStatus status) {
        logger.debug("Getting employees by status: {}", status);
        return employeeMapper.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Employee> searchEmployees(String keyword) {
        logger.debug("Searching employees with keyword: {}", keyword);
        if (keyword == null || keyword.trim().isEmpty()) {
            return employeeMapper.findAll();
        }
        return employeeMapper.search(keyword.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Employee> searchEmployeesWithDepartment(String keyword) {
        logger.debug("Searching employees with department and keyword: {}", keyword);
        if (keyword == null || keyword.trim().isEmpty()) {
            return employeeMapper.findAllWithDepartment();
        }
        return employeeMapper.searchWithDepartment(keyword.trim());
    }

    @Override
    public Employee createEmployee(EmployeeDTO employeeDTO) {
        logger.info("Creating new employee: {}", employeeDTO.getEmployeeCode());

        if (existsByCode(employeeDTO.getEmployeeCode())) {
            throw new IllegalArgumentException("Employee code '" + employeeDTO.getEmployeeCode() + "' already exists");
        }

        if (existsByEmail(employeeDTO.getEmail())) {
            throw new IllegalArgumentException("Email '" + employeeDTO.getEmail() + "' already exists");
        }

        Employee employee = toEntity(employeeDTO);
        employeeMapper.insert(employee);
        logger.info("Employee created with id: {}", employee.getId());
        return employee;
    }

    @Override
    public Employee updateEmployee(Long id, EmployeeDTO employeeDTO) {
        logger.info("Updating employee with id: {}", id);

        Employee existing = employeeMapper.findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Employee with id " + id + " not found");
        }

        // Check unique constraints
        Employee codeCheck = employeeMapper.findByEmployeeCode(employeeDTO.getEmployeeCode());
        if (codeCheck != null && !codeCheck.getId().equals(id)) {
            throw new IllegalArgumentException("Employee code '" + employeeDTO.getEmployeeCode() + "' already exists");
        }

        Employee emailCheck = employeeMapper.findByEmail(employeeDTO.getEmail());
        if (emailCheck != null && !emailCheck.getId().equals(id)) {
            throw new IllegalArgumentException("Email '" + employeeDTO.getEmail() + "' already exists");
        }

        Employee employee = toEntity(employeeDTO);
        employee.setId(id);
        employeeMapper.update(employee);
        logger.info("Employee updated: {}", id);
        return employeeMapper.findById(id);
    }

    @Override
    public boolean deleteEmployee(Long id) {
        logger.info("Deleting employee with id: {}", id);

        if (!existsById(id)) {
            throw new IllegalArgumentException("Employee with id " + id + " not found");
        }

        int deleted = employeeMapper.deleteById(id);
        logger.info("Employee deleted: {}", id);
        return deleted > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return employeeMapper.findById(id) != null;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCode(String employeeCode) {
        return employeeMapper.findByEmployeeCode(employeeCode) != null;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return employeeMapper.findByEmail(email) != null;
    }

    @Override
    @Transactional(readOnly = true)
    public long countEmployees() {
        return employeeMapper.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countEmployeesByStatus(EmployeeStatus status) {
        return employeeMapper.countByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public long countEmployeesByDepartment(Long departmentId) {
        return employeeMapper.countByDepartment(departmentId);
    }

    @Override
    public EmployeeDTO toDTO(Employee employee) {
        if (employee == null) {
            return null;
        }

        EmployeeDTO dto = EmployeeDTO.builder()
                .id(employee.getId())
                .employeeCode(employee.getEmployeeCode())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .phone(employee.getPhone())
                .dateOfBirth(employee.getDateOfBirth())
                .hireDate(employee.getHireDate())
                .salary(employee.getSalary())
                .departmentId(employee.getDepartmentId())
                .position(employee.getPosition())
                .status(employee.getStatus())
                .address(employee.getAddress())
                .city(employee.getCity())
                .country(employee.getCountry())
                .build();

        if (employee.getDepartment() != null) {
            dto.setDepartmentName(employee.getDepartment().getName());
        }

        return dto;
    }

    @Override
    public Employee toEntity(EmployeeDTO dto) {
        if (dto == null) {
            return null;
        }

        return Employee.builder()
                .id(dto.getId())
                .employeeCode(dto.getEmployeeCode())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .dateOfBirth(dto.getDateOfBirth())
                .hireDate(dto.getHireDate())
                .salary(dto.getSalary())
                .departmentId(dto.getDepartmentId())
                .position(dto.getPosition())
                .status(dto.getStatus() != null ? dto.getStatus() : EmployeeStatus.ACTIVE)
                .address(dto.getAddress())
                .city(dto.getCity())
                .country(dto.getCountry())
                .build();
    }
}

