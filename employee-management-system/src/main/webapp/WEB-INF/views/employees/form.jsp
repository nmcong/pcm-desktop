<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="${isEdit ? 'Chỉnh sửa Nhân viên' : 'Thêm Nhân viên mới'}" scope="request"/>
<jsp:include page="../common/header.jsp"/>

<div class="row">
    <div class="col-12">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h1>
                <i class="bi ${isEdit ? 'bi-pencil-square' : 'bi-person-plus-fill'}"></i> 
                ${isEdit ? 'Chỉnh sửa Nhân viên' : 'Thêm Nhân viên mới'}
            </h1>
            <a href="<c:url value='/employees'/>" class="btn btn-secondary">
                <i class="bi bi-arrow-left"></i> Quay lại
            </a>
        </div>
    </div>
</div>

<!-- Error Messages -->
<c:if test="${not empty error}">
    <div class="alert alert-danger alert-dismissible fade show" role="alert">
        <i class="bi bi-exclamation-triangle-fill"></i> ${error}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
</c:if>

<!-- Employee Form -->
<div class="row">
    <div class="col-12">
        <div class="card">
            <div class="card-body">
                <form action="<c:url value='${isEdit ? "/employees/update/".concat(employeeDTO.id) : "/employees"}'/>" 
                      method="post" id="employeeForm">
                    
                    <!-- Basic Information -->
                    <h5 class="mb-3 text-primary">
                        <i class="bi bi-info-circle"></i> Thông tin cơ bản
                    </h5>
                    
                    <div class="row mb-3">
                        <div class="col-md-4">
                            <label for="employeeCode" class="form-label">Mã nhân viên <span class="text-danger">*</span></label>
                            <input type="text" class="form-control" id="employeeCode" name="employeeCode" 
                                   value="${employeeDTO.employeeCode}" required>
                            <div class="invalid-feedback" id="employeeCodeError"></div>
                        </div>
                        
                        <div class="col-md-4">
                            <label for="firstName" class="form-label">Tên <span class="text-danger">*</span></label>
                            <input type="text" class="form-control" id="firstName" name="firstName" 
                                   value="${employeeDTO.firstName}" required>
                        </div>
                        
                        <div class="col-md-4">
                            <label for="lastName" class="form-label">Họ <span class="text-danger">*</span></label>
                            <input type="text" class="form-control" id="lastName" name="lastName" 
                                   value="${employeeDTO.lastName}" required>
                        </div>
                    </div>
                    
                    <div class="row mb-3">
                        <div class="col-md-6">
                            <label for="email" class="form-label">Email <span class="text-danger">*</span></label>
                            <input type="email" class="form-control" id="email" name="email" 
                                   value="${employeeDTO.email}" required>
                            <div class="invalid-feedback" id="emailError"></div>
                        </div>
                        
                        <div class="col-md-6">
                            <label for="phone" class="form-label">Số điện thoại</label>
                            <input type="tel" class="form-control" id="phone" name="phone" 
                                   value="${employeeDTO.phone}" pattern="[0-9]{10,11}">
                            <small class="form-text text-muted">Định dạng: 10-11 chữ số</small>
                        </div>
                    </div>
                    
                    <!-- Employment Information -->
                    <h5 class="mb-3 mt-4 text-primary">
                        <i class="bi bi-briefcase"></i> Thông tin công việc
                    </h5>
                    
                    <div class="row mb-3">
                        <div class="col-md-4">
                            <label for="departmentId" class="form-label">Phòng ban</label>
                            <select class="form-select" id="departmentId" name="departmentId">
                                <option value="">-- Chọn phòng ban --</option>
                                <c:forEach items="${departments}" var="dept">
                                    <option value="${dept.id}" ${employeeDTO.departmentId eq dept.id ? 'selected' : ''}>
                                        ${dept.name}
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                        
                        <div class="col-md-4">
                            <label for="position" class="form-label">Chức vụ</label>
                            <input type="text" class="form-control" id="position" name="position" 
                                   value="${employeeDTO.position}">
                        </div>
                        
                        <div class="col-md-4">
                            <label for="status" class="form-label">Trạng thái <span class="text-danger">*</span></label>
                            <select class="form-select" id="status" name="status" required>
                                <c:forEach items="${statuses}" var="st">
                                    <option value="${st}" ${employeeDTO.status eq st ? 'selected' : ''}>
                                        ${st.displayName}
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>
                    
                    <div class="row mb-3">
                        <div class="col-md-4">
                            <label for="hireDate" class="form-label">Ngày vào làm <span class="text-danger">*</span></label>
                            <input type="date" class="form-control" id="hireDate" name="hireDate" 
                                   value="${employeeDTO.hireDate}" required>
                        </div>
                        
                        <div class="col-md-4">
                            <label for="dateOfBirth" class="form-label">Ngày sinh</label>
                            <input type="date" class="form-control" id="dateOfBirth" name="dateOfBirth" 
                                   value="${employeeDTO.dateOfBirth}">
                        </div>
                        
                        <div class="col-md-4">
                            <label for="salary" class="form-label">Lương (VNĐ)</label>
                            <input type="number" class="form-control" id="salary" name="salary" 
                                   value="${employeeDTO.salary}" min="0" step="1000">
                        </div>
                    </div>
                    
                    <!-- Address Information -->
                    <h5 class="mb-3 mt-4 text-primary">
                        <i class="bi bi-geo-alt"></i> Thông tin địa chỉ
                    </h5>
                    
                    <div class="row mb-3">
                        <div class="col-md-12">
                            <label for="address" class="form-label">Địa chỉ</label>
                            <input type="text" class="form-control" id="address" name="address" 
                                   value="${employeeDTO.address}">
                        </div>
                    </div>
                    
                    <div class="row mb-3">
                        <div class="col-md-6">
                            <label for="city" class="form-label">Thành phố</label>
                            <input type="text" class="form-control" id="city" name="city" 
                                   value="${employeeDTO.city}">
                        </div>
                        
                        <div class="col-md-6">
                            <label for="country" class="form-label">Quốc gia</label>
                            <input type="text" class="form-control" id="country" name="country" 
                                   value="${employeeDTO.country}">
                        </div>
                    </div>
                    
                    <!-- Form Actions -->
                    <div class="row mt-4">
                        <div class="col-12">
                            <button type="submit" class="btn btn-primary btn-lg">
                                <i class="bi bi-save"></i> ${isEdit ? 'Cập nhật' : 'Tạo mới'}
                            </button>
                            <a href="<c:url value='/employees'/>" class="btn btn-secondary btn-lg">
                                <i class="bi bi-x-circle"></i> Hủy
                            </a>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<script>
$(document).ready(function() {
    const isEdit = ${isEdit ? 'true' : 'false'};
    const employeeId = ${employeeDTO.id != null ? employeeDTO.id : 'null'};
    
    // Validate employee code
    $('#employeeCode').on('blur', function() {
        const code = $(this).val().trim();
        if (code) {
            $.ajax({
                url: '<c:url value="/employees/api/check-code"/>',
                data: { 
                    code: code,
                    excludeId: employeeId
                },
                success: function(response) {
                    if (response.exists) {
                        $('#employeeCode').addClass('is-invalid');
                        $('#employeeCodeError').text('Mã nhân viên đã tồn tại');
                    } else {
                        $('#employeeCode').removeClass('is-invalid').addClass('is-valid');
                    }
                }
            });
        }
    });
    
    // Validate email
    $('#email').on('blur', function() {
        const email = $(this).val().trim();
        if (email) {
            $.ajax({
                url: '<c:url value="/employees/api/check-email"/>',
                data: { 
                    email: email,
                    excludeId: employeeId
                },
                success: function(response) {
                    if (response.exists) {
                        $('#email').addClass('is-invalid');
                        $('#emailError').text('Email đã tồn tại');
                    } else {
                        $('#email').removeClass('is-invalid').addClass('is-valid');
                    }
                }
            });
        }
    });
    
    // Form validation on submit
    $('#employeeForm').on('submit', function(e) {
        if ($('.is-invalid').length > 0) {
            e.preventDefault();
            alert('Vui lòng kiểm tra lại các trường thông tin!');
            return false;
        }
    });
});
</script>

<jsp:include page="../common/footer.jsp"/>

