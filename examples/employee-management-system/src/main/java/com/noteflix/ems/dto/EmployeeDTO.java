package com.noteflix.ems.dto;

import com.noteflix.ems.model.EmployeeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data Transfer Object for Employee
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDTO {
    private Long id;

    @NotBlank(message = "Mã nhân viên không được để trống")
    @Size(max = 20, message = "Mã nhân viên không được quá 20 ký tự")
    private String employeeCode;

    @NotBlank(message = "Tên không được để trống")
    @Size(max = 50, message = "Tên không được quá 50 ký tự")
    private String firstName;

    @NotBlank(message = "Họ không được để trống")
    @Size(max = 50, message = "Họ không được quá 50 ký tự")
    private String lastName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại phải có 10-11 chữ số")
    private String phone;

    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    private LocalDate dateOfBirth;

    @NotNull(message = "Ngày vào làm không được để trống")
    private LocalDate hireDate;

    @DecimalMin(value = "0.0", inclusive = false, message = "Lương phải lớn hơn 0")
    private BigDecimal salary;

    private Long departmentId;

    @Size(max = 100, message = "Chức vụ không được quá 100 ký tự")
    private String position;

    private EmployeeStatus status;

    @Size(max = 255, message = "Địa chỉ không được quá 255 ký tự")
    private String address;

    @Size(max = 50, message = "Thành phố không được quá 50 ký tự")
    private String city;

    @Size(max = 50, message = "Quốc gia không được quá 50 ký tự")
    private String country;

    // For display purposes
    private String departmentName;
}

