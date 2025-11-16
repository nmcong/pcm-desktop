<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Trang chủ - Hệ thống Quản lý Nhân sự" scope="request"/>
<jsp:include page="common/header.jsp"/>

<div class="row">
    <div class="col-12">
        <h1 class="mb-4">
            <i class="bi bi-speedometer2"></i> Dashboard
        </h1>
    </div>
</div>

<!-- Statistics Cards -->
<div class="row">
    <div class="col-md-4 mb-4">
        <div class="card bg-primary text-white h-100">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <h6 class="card-title text-uppercase mb-2">Tổng nhân viên</h6>
                        <h2 class="mb-0">${totalEmployees}</h2>
                    </div>
                    <div class="text-white-50">
                        <i class="bi bi-people-fill" style="font-size: 3rem;"></i>
                    </div>
                </div>
            </div>
            <div class="card-footer bg-primary bg-opacity-75">
                <a href="<c:url value='/employees'/>" class="text-white text-decoration-none">
                    <small>Xem chi tiết <i class="bi bi-arrow-right"></i></small>
                </a>
            </div>
        </div>
    </div>

    <div class="col-md-4 mb-4">
        <div class="card bg-success text-white h-100">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <h6 class="card-title text-uppercase mb-2">Đang làm việc</h6>
                        <h2 class="mb-0">${activeEmployees}</h2>
                    </div>
                    <div class="text-white-50">
                        <i class="bi bi-check-circle-fill" style="font-size: 3rem;"></i>
                    </div>
                </div>
            </div>
            <div class="card-footer bg-success bg-opacity-75">
                <a href="<c:url value='/employees?status=ACTIVE'/>" class="text-white text-decoration-none">
                    <small>Xem danh sách <i class="bi bi-arrow-right"></i></small>
                </a>
            </div>
        </div>
    </div>

    <div class="col-md-4 mb-4">
        <div class="card bg-info text-white h-100">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <h6 class="card-title text-uppercase mb-2">Phòng ban</h6>
                        <h2 class="mb-0">${totalDepartments}</h2>
                    </div>
                    <div class="text-white-50">
                        <i class="bi bi-building" style="font-size: 3rem;"></i>
                    </div>
                </div>
            </div>
            <div class="card-footer bg-info bg-opacity-75">
                <a href="<c:url value='/departments'/>" class="text-white text-decoration-none">
                    <small>Xem danh sách <i class="bi bi-arrow-right"></i></small>
                </a>
            </div>
        </div>
    </div>
</div>

<!-- Quick Actions -->
<div class="row mt-4">
    <div class="col-12">
        <div class="card">
            <div class="card-header bg-white">
                <h5 class="mb-0">
                    <i class="bi bi-lightning-charge-fill"></i> Thao tác nhanh
                </h5>
            </div>
            <div class="card-body">
                <div class="row">
                    <div class="col-md-3 mb-3">
                        <a href="<c:url value='/employees/new'/>" class="btn btn-primary btn-lg w-100">
                            <i class="bi bi-person-plus-fill"></i><br/>
                            Thêm nhân viên mới
                        </a>
                    </div>
                    <div class="col-md-3 mb-3">
                        <a href="<c:url value='/employees'/>" class="btn btn-info btn-lg w-100">
                            <i class="bi bi-list-ul"></i><br/>
                            Danh sách nhân viên
                        </a>
                    </div>
                    <div class="col-md-3 mb-3">
                        <a href="<c:url value='/departments'/>" class="btn btn-success btn-lg w-100">
                            <i class="bi bi-building"></i><br/>
                            Quản lý phòng ban
                        </a>
                    </div>
                    <div class="col-md-3 mb-3">
                        <button type="button" class="btn btn-warning btn-lg w-100" id="searchBtn">
                            <i class="bi bi-search"></i><br/>
                            Tìm kiếm
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Welcome Message -->
<div class="row mt-4">
    <div class="col-12">
        <div class="alert alert-info">
            <h4 class="alert-heading">
                <i class="bi bi-info-circle-fill"></i> Chào mừng!
            </h4>
            <p class="mb-0">
                Đây là hệ thống quản lý nhân sự. Bạn có thể quản lý thông tin nhân viên, phòng ban và các chức năng liên
                quan khác.
            </p>
        </div>
    </div>
</div>

<script>
    $(document).ready(function () {
        $('#searchBtn').click(function () {
            window.location.href = '<c:url value="/employees"/>';
        });
    });
</script>

<jsp:include page="common/footer.jsp"/>

