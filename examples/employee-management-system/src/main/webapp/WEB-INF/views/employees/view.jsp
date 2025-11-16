<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="custom" uri="http://noteflix.com/jsp/custom" %>
<c:set var="pageTitle" value="Chi tiết Nhân viên" scope="request"/>
<jsp:include page="../common/header.jsp"/>

<div class="row">
    <div class="col-12">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h1>
                <i class="bi bi-person-circle"></i> Chi tiết Nhân viên
            </h1>
            <div>
                <a href="<c:url value='/employees/edit/${employee.id}'/>" class="btn btn-warning">
                    <i class="bi bi-pencil"></i> Chỉnh sửa
                </a>
                <a href="<c:url value='/employees'/>" class="btn btn-secondary">
                    <i class="bi bi-arrow-left"></i> Quay lại
                </a>
            </div>
        </div>
    </div>
</div>

<div class="row">
    <!-- Left Column - Basic Info -->
    <div class="col-md-6 mb-4">
        <div class="card h-100">
            <div class="card-header bg-primary text-white">
                <h5 class="mb-0">
                    <i class="bi bi-info-circle"></i> Thông tin cơ bản
                </h5>
            </div>
            <div class="card-body">
                <div class="row mb-3">
                    <div class="col-4 fw-bold">Mã nhân viên:</div>
                    <div class="col-8">
                        <span class="badge bg-primary fs-6">${employee.employeeCode}</span>
                    </div>
                </div>

                <div class="row mb-3">
                    <div class="col-4 fw-bold">Họ và tên:</div>
                    <div class="col-8 fs-5 text-primary">${employee.fullName}</div>
                </div>

                <div class="row mb-3">
                    <div class="col-4 fw-bold">Email:</div>
                    <div class="col-8">
                        <a href="mailto:${employee.email}">${employee.email}</a>
                    </div>
                </div>

                <div class="row mb-3">
                    <div class="col-4 fw-bold">Số điện thoại:</div>
                    <div class="col-8">
                        <c:choose>
                            <c:when test="${not empty employee.phone}">
                                <a href="tel:${employee.phone}">${employee.phone}</a>
                            </c:when>
                            <c:otherwise>
                                <span class="text-muted">Chưa có</span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <div class="row mb-3">
                    <div class="col-4 fw-bold">Ngày sinh:</div>
                    <div class="col-8">
                        <c:choose>
                            <c:when test="${not empty employee.dateOfBirth}">
                                ${custom:formatDate(employee.dateOfBirth)}
                            </c:when>
                            <c:otherwise>
                                <span class="text-muted">Chưa có</span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <div class="row mb-3">
                    <div class="col-4 fw-bold">Trạng thái:</div>
                    <div class="col-8">
                        <c:choose>
                            <c:when test="${employee.status eq 'ACTIVE'}">
                                <span class="badge bg-success fs-6">Đang làm việc</span>
                            </c:when>
                            <c:when test="${employee.status eq 'INACTIVE'}">
                                <span class="badge bg-secondary fs-6">Nghỉ việc</span>
                            </c:when>
                            <c:when test="${employee.status eq 'ON_LEAVE'}">
                                <span class="badge bg-warning fs-6">Nghỉ phép</span>
                            </c:when>
                            <c:otherwise>
                                <span class="badge bg-danger fs-6">Tạm ngưng</span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Right Column - Work Info -->
    <div class="col-md-6 mb-4">
        <div class="card h-100">
            <div class="card-header bg-success text-white">
                <h5 class="mb-0">
                    <i class="bi bi-briefcase"></i> Thông tin công việc
                </h5>
            </div>
            <div class="card-body">
                <div class="row mb-3">
                    <div class="col-4 fw-bold">Phòng ban:</div>
                    <div class="col-8">
                        <c:choose>
                            <c:when test="${not empty employee.department}">
                                <span class="badge bg-info fs-6">${employee.department.name}</span>
                            </c:when>
                            <c:otherwise>
                                <span class="text-muted">Chưa phân công</span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <div class="row mb-3">
                    <div class="col-4 fw-bold">Chức vụ:</div>
                    <div class="col-8">
                        <c:choose>
                            <c:when test="${not empty employee.position}">
                                ${employee.position}
                            </c:when>
                            <c:otherwise>
                                <span class="text-muted">Chưa có</span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <div class="row mb-3">
                    <div class="col-4 fw-bold">Ngày vào làm:</div>
                    <div class="col-8">
                        ${custom:formatDate(employee.hireDate)}
                    </div>
                </div>

                <div class="row mb-3">
                    <div class="col-4 fw-bold">Lương:</div>
                    <div class="col-8">
                        <c:choose>
                            <c:when test="${not empty employee.salary}">
                                <span class="text-success fw-bold">
                                    <fmt:formatNumber value="${employee.salary}" type="currency"
                                                      currencySymbol="₫" groupingUsed="true"/>
                                </span>
                            </c:when>
                            <c:otherwise>
                                <span class="text-muted">Chưa có</span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Address Information -->
<div class="row">
    <div class="col-12 mb-4">
        <div class="card">
            <div class="card-header bg-info text-white">
                <h5 class="mb-0">
                    <i class="bi bi-geo-alt"></i> Thông tin địa chỉ
                </h5>
            </div>
            <div class="card-body">
                <div class="row mb-3">
                    <div class="col-md-2 fw-bold">Địa chỉ:</div>
                    <div class="col-md-10">
                        <c:choose>
                            <c:when test="${not empty employee.address}">
                                ${employee.address}
                            </c:when>
                            <c:otherwise>
                                <span class="text-muted">Chưa có</span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <div class="row mb-3">
                    <div class="col-md-2 fw-bold">Thành phố:</div>
                    <div class="col-md-4">${not empty employee.city ? employee.city : '<span class="text-muted">Chưa có</span>'}</div>

                    <div class="col-md-2 fw-bold">Quốc gia:</div>
                    <div class="col-md-4">${not empty employee.country ? employee.country : '<span class="text-muted">Chưa có</span>'}</div>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Timestamps -->
<div class="row">
    <div class="col-12">
        <div class="card">
            <div class="card-body bg-light">
                <div class="row">
                    <div class="col-md-6">
                        <small class="text-muted">
                            <i class="bi bi-calendar-plus"></i> Ngày tạo:
                            ${custom:formatDateTime(employee.createdAt)}
                        </small>
                    </div>
                    <div class="col-md-6 text-end">
                        <small class="text-muted">
                            <i class="bi bi-calendar-check"></i> Cập nhật lần cuối:
                            ${custom:formatDateTime(employee.updatedAt)}
                        </small>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../common/footer.jsp"/>

