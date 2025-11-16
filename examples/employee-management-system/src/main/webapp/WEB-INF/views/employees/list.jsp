<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Danh sách Nhân viên" scope="request"/>
<jsp:include page="../common/header.jsp"/>

<div class="row">
    <div class="col-12">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h1>
                <i class="bi bi-person-badge"></i> Danh sách Nhân viên
            </h1>
            <a href="<c:url value='/employees/new'/>" class="btn btn-primary">
                <i class="bi bi-plus-circle"></i> Thêm nhân viên mới
            </a>
        </div>
    </div>
</div>

<!-- Flash Messages -->
<c:if test="${not empty success}">
    <div class="alert alert-success alert-dismissible fade show" role="alert">
        <i class="bi bi-check-circle-fill"></i> ${success}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
</c:if>
<c:if test="${not empty error}">
    <div class="alert alert-danger alert-dismissible fade show" role="alert">
        <i class="bi bi-exclamation-triangle-fill"></i> ${error}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
</c:if>

<!-- Search and Filter -->
<div class="row mb-3">
    <div class="col-12">
        <div class="card">
            <div class="card-body">
                <form action="<c:url value='/employees'/>" method="get" class="row g-3">
                    <div class="col-md-10">
                        <div class="input-group">
                            <span class="input-group-text">
                                <i class="bi bi-search"></i>
                            </span>
                            <input type="text" class="form-control" name="search"
                                   placeholder="Tìm kiếm theo tên, email, mã nhân viên..."
                                   value="${search}">
                        </div>
                    </div>
                    <div class="col-md-2">
                        <button type="submit" class="btn btn-primary w-100">
                            <i class="bi bi-search"></i> Tìm kiếm
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<!-- Employee Table -->
<div class="row">
    <div class="col-12">
        <div class="card">
            <div class="card-body">
                <div class="table-responsive">
                    <table class="table table-striped table-hover" id="employeeTable">
                        <thead class="table-dark">
                        <tr>
                            <th>Mã NV</th>
                            <th>Họ và tên</th>
                            <th>Email</th>
                            <th>Số điện thoại</th>
                            <th>Phòng ban</th>
                            <th>Chức vụ</th>
                            <th>Trạng thái</th>
                            <th>Thao tác</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:choose>
                            <c:when test="${empty employees}">
                                <tr>
                                    <td colspan="8" class="text-center py-4">
                                        <i class="bi bi-inbox" style="font-size: 3rem;"></i>
                                        <p class="mt-2">Không có nhân viên nào</p>
                                    </td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach items="${employees}" var="employee">
                                    <tr>
                                        <td><strong>${employee.employeeCode}</strong></td>
                                        <td>${employee.fullName}</td>
                                        <td>${employee.email}</td>
                                        <td>${employee.phone}</td>
                                        <td>
                                            <c:if test="${not empty employee.department}">
                                                <span class="badge bg-info">${employee.department.name}</span>
                                            </c:if>
                                        </td>
                                        <td>${employee.position}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${employee.status eq 'ACTIVE'}">
                                                    <span class="badge bg-success">Đang làm việc</span>
                                                </c:when>
                                                <c:when test="${employee.status eq 'INACTIVE'}">
                                                    <span class="badge bg-secondary">Nghỉ việc</span>
                                                </c:when>
                                                <c:when test="${employee.status eq 'ON_LEAVE'}">
                                                    <span class="badge bg-warning">Nghỉ phép</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge bg-danger">Tạm ngưng</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <div class="btn-group" role="group">
                                                <a href="<c:url value='/employees/view/${employee.id}'/>"
                                                   class="btn btn-sm btn-info" title="Xem chi tiết">
                                                    <i class="bi bi-eye"></i>
                                                </a>
                                                <a href="<c:url value='/employees/edit/${employee.id}'/>"
                                                   class="btn btn-sm btn-warning" title="Chỉnh sửa">
                                                    <i class="bi bi-pencil"></i>
                                                </a>
                                                <button type="button" class="btn btn-sm btn-danger delete-btn"
                                                        data-id="${employee.id}"
                                                        data-name="${employee.fullName}"
                                                        title="Xóa">
                                                    <i class="bi bi-trash"></i>
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Delete Confirmation Modal -->
<div class="modal fade" id="deleteModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Xác nhận xóa</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <p>Bạn có chắc chắn muốn xóa nhân viên <strong id="employeeName"></strong>?</p>
                <p class="text-danger"><small>Hành động này không thể hoàn tác!</small></p>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
                <form id="deleteForm" method="post" style="display: inline;">
                    <button type="submit" class="btn btn-danger">
                        <i class="bi bi-trash"></i> Xóa
                    </button>
                </form>
            </div>
        </div>
    </div>
</div>

<script>
    $(document).ready(function () {
        // Handle delete button click
        $('.delete-btn').click(function () {
            const employeeId = $(this).data('id');
            const employeeName = $(this).data('name');

            $('#employeeName').text(employeeName);
            $('#deleteForm').attr('action', '<c:url value="/employees/delete/"/>' + employeeId);

            new bootstrap.Modal($('#deleteModal')).show();
        });
    });
</script>

<jsp:include page="../common/footer.jsp"/>

