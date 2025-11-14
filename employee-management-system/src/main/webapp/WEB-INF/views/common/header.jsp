<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${pageTitle != null ? pageTitle : 'Hệ thống Quản lý Nhân sự'}</title>
    
    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Bootstrap Icons -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css" rel="stylesheet">
    <!-- Custom CSS -->
    <link href="<c:url value='/resources/css/style.css'/>" rel="stylesheet">
</head>
<body>
    <!-- Navigation -->
    <nav class="navbar navbar-expand-lg navbar-dark bg-primary">
        <div class="container-fluid">
            <a class="navbar-brand" href="<c:url value='/'/>">
                <i class="bi bi-people-fill"></i> Quản lý Nhân sự
            </a>
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse" id="navbarNav">
                <ul class="navbar-nav ms-auto">
                    <li class="nav-item">
                        <a class="nav-link ${pageContext.request.requestURI eq '/' ? 'active' : ''}" 
                           href="<c:url value='/'/>">
                            <i class="bi bi-house-door"></i> Trang chủ
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link ${pageContext.request.requestURI.contains('/employees') ? 'active' : ''}" 
                           href="<c:url value='/employees'/>">
                            <i class="bi bi-person-badge"></i> Nhân viên
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link ${pageContext.request.requestURI.contains('/departments') ? 'active' : ''}" 
                           href="<c:url value='/departments'/>">
                            <i class="bi bi-building"></i> Phòng ban
                        </a>
                    </li>
                </ul>
            </div>
        </div>
    </nav>
    
    <!-- Main Content Container -->
    <div class="container-fluid mt-4">

