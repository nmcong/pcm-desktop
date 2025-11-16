<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Danh sách Phòng ban" scope="request"/>
<jsp:include page="../common/header.jsp"/>

<div class="row">
    <div class="col-12">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h1>
                <i class="bi bi-building"></i> Danh sách Phòng ban
            </h1>
        </div>
    </div>
</div>

<!-- Departments Grid -->
<div class="row">
    <c:choose>
        <c:when test="${empty departments}">
            <div class="col-12">
                <div class="alert alert-info text-center">
                    <i class="bi bi-inbox" style="font-size: 3rem;"></i>
                    <p class="mt-2">Không có phòng ban nào</p>
                </div>
            </div>
        </c:when>
        <c:otherwise>
            <c:forEach items="${departments}" var="dept">
                <div class="col-md-6 col-lg-4 mb-4">
                    <div class="card h-100 shadow-sm department-card">
                        <div class="card-body">
                            <h5 class="card-title text-primary">
                                <i class="bi bi-building"></i> ${dept.name}
                            </h5>
                            <p class="card-text text-muted">
                                <c:choose>
                                    <c:when test="${not empty dept.description}">
                                        ${dept.description}
                                    </c:when>
                                    <c:otherwise>
                                        <em>Chưa có mô tả</em>
                                    </c:otherwise>
                                </c:choose>
                            </p>
                        </div>
                        <div class="card-footer bg-transparent">
                            <a href="<c:url value='/employees?departmentId=${dept.id}'/>"
                               class="btn btn-sm btn-primary">
                                <i class="bi bi-people"></i> Xem nhân viên
                            </a>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </c:otherwise>
    </c:choose>
</div>

<style>
    .department-card {
        transition: transform 0.2s, box-shadow 0.2s;
    }

    .department-card:hover {
        transform: translateY(-5px);
        box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.15) !important;
    }
</style>

<jsp:include page="../common/footer.jsp"/>

