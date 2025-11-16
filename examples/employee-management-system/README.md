# Hệ thống Quản lý Nhân sự (Employee Management System)

Hệ thống quản lý nhân sự được xây dựng bằng Spring MVC, MyBatis, H2 Database, JSP và jQuery.

## Tính năng

### 1. Quản lý Nhân viên

- ✅ Thêm nhân viên mới
- ✅ Xem danh sách nhân viên
- ✅ Xem chi tiết nhân viên
- ✅ Chỉnh sửa thông tin nhân viên
- ✅ Xóa nhân viên
- ✅ Tìm kiếm nhân viên (theo tên, email, mã nhân viên)
- ✅ Lọc nhân viên theo phòng ban
- ✅ Validation dữ liệu (mã nhân viên, email unique)

### 2. Quản lý Phòng ban

- ✅ Xem danh sách phòng ban
- ✅ Xem nhân viên theo phòng ban

### 3. Dashboard

- ✅ Thống kê tổng số nhân viên
- ✅ Thống kê nhân viên đang làm việc
- ✅ Thống kê số phòng ban
- ✅ Quick actions

## Công nghệ sử dụng

### Backend

- **Spring MVC 6.0.13** - Web Framework
- **MyBatis 3.5.13** - ORM Framework
- **H2 Database 2.2.224** - In-memory Database
- **Maven** - Build Tool
- **Java 17** - Programming Language

### Frontend

- **JSP & JSTL** - View Technology
- **Bootstrap 5.3** - CSS Framework
- **jQuery 3.7.1** - JavaScript Library
- **Bootstrap Icons** - Icon Library

### Other

- **Lombok** - Boilerplate Code Reduction
- **Logback** - Logging Framework
- **Jakarta Validation** - Bean Validation

## Cấu trúc dự án

```
employee-management-system/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/noteflix/ems/
│   │   │       ├── controller/      # Controllers
│   │   │       ├── service/         # Service layer
│   │   │       │   └── impl/        # Service implementations
│   │   │       ├── mapper/          # MyBatis Mappers
│   │   │       ├── model/           # Entity classes
│   │   │       └── dto/             # Data Transfer Objects
│   │   ├── resources/
│   │   │   ├── mapper/              # MyBatis XML mappers
│   │   │   ├── mybatis-config.xml   # MyBatis configuration
│   │   │   ├── schema.sql           # Database schema
│   │   │   ├── data.sql             # Sample data
│   │   │   ├── logback.xml          # Logging configuration
│   │   │   └── messages.properties  # i18n messages
│   │   └── webapp/
│   │       ├── WEB-INF/
│   │       │   ├── views/           # JSP views
│   │       │   │   ├── common/      # Header, footer
│   │       │   │   ├── employees/   # Employee views
│   │       │   │   └── departments/ # Department views
│   │       │   ├── web.xml          # Web application configuration
│   │       │   └── spring-mvc-config.xml  # Spring configuration
│   │       ├── resources/
│   │       │   ├── css/             # CSS files
│   │       │   └── js/              # JavaScript files
│   │       └── index.jsp            # Home page
│   └── test/
│       └── java/                    # Unit tests (to be added)
└── pom.xml                          # Maven configuration
```

## Yêu cầu hệ thống

- **Java**: JDK 17 hoặc cao hơn
- **Maven**: 3.6+ (để build)
- **Container**: Servlet 6.0 compatible (Tomcat 10+, Jetty 11+)

## Cài đặt và chạy

### 1. Clone hoặc copy dự án

```bash
cd employee-management-system
```

### 2. Build dự án

```bash
mvn clean install
```

### 3. Chạy với Jetty (Embedded Server)

```bash
mvn jetty:run
```

Ứng dụng sẽ chạy tại: `http://localhost:8080/employee-management`

### 4. Deploy trên Tomcat

Build file WAR:

```bash
mvn clean package
```

Copy file `target/employee-management.war` vào thư mục `webapps` của Tomcat và khởi động Tomcat.

## Cấu hình Database

Dự án sử dụng **H2 in-memory database** nên không cần cài đặt database riêng. Database sẽ được khởi tạo tự động khi ứng
dụng chạy.

### Cấu hình hiện tại (trong spring-mvc-config.xml):

```xml
<bean id="dataSource" class="org.springframework.jdbc.datasource.DriverManagerDataSource">
    <property name="driverClassName" value="org.h2.Driver"/>
    <property name="url" value="jdbc:h2:mem:employeedb"/>
    <property name="username" value="sa"/>
    <property name="password" value=""/>
</bean>
```

### Truy cập H2 Console (tùy chọn):

Nếu muốn xem database qua H2 Console, thêm dependency sau vào `pom.xml` và enable H2 console trong Spring config.

## Sample Data

Hệ thống đã có sẵn dữ liệu mẫu:

- **6 phòng ban**: Kỹ thuật, Nhân sự, Kế toán, Marketing, Kinh doanh, Hành chính
- **10 nhân viên** với đầy đủ thông tin

## API Endpoints

### Web Pages (JSP)

- `GET /` - Dashboard/Trang chủ
- `GET /employees` - Danh sách nhân viên
- `GET /employees/new` - Form thêm nhân viên
- `GET /employees/edit/{id}` - Form sửa nhân viên
- `GET /employees/view/{id}` - Chi tiết nhân viên
- `POST /employees` - Tạo nhân viên mới
- `POST /employees/update/{id}` - Cập nhật nhân viên
- `POST /employees/delete/{id}` - Xóa nhân viên
- `GET /departments` - Danh sách phòng ban

### REST API (AJAX)

- `GET /employees/api/{id}` - Lấy thông tin nhân viên (JSON)
- `GET /employees/api/search?keyword=` - Tìm kiếm nhân viên (JSON)
- `GET /employees/api/check-code?code=&excludeId=` - Kiểm tra mã nhân viên
- `GET /employees/api/check-email?email=&excludeId=` - Kiểm tra email
- `GET /departments/api` - Lấy danh sách phòng ban (JSON)
- `GET /departments/api/{id}` - Lấy thông tin phòng ban (JSON)

## Tính năng nổi bật

### 1. Validation

- ✅ Server-side validation với Jakarta Validation
- ✅ Client-side validation với jQuery
- ✅ Real-time validation (check unique constraints)

### 2. User Experience

- ✅ Responsive design với Bootstrap 5
- ✅ Flash messages (success, error)
- ✅ Confirmation dialogs
- ✅ Auto-hide alerts
- ✅ Loading indicators
- ✅ Smooth animations

### 3. Search & Filter

- ✅ Tìm kiếm theo multiple fields
- ✅ Lọc theo phòng ban
- ✅ Debounced search

### 4. Code Quality

- ✅ Clean architecture (Controller → Service → Mapper)
- ✅ DTOs for data transfer
- ✅ Logging với Logback
- ✅ Transaction management
- ✅ Exception handling

## Mở rộng trong tương lai

- [ ] Authentication & Authorization (Spring Security)
- [ ] Phân quyền người dùng
- [ ] Quản lý chấm công
- [ ] Quản lý lương thưởng
- [ ] Quản lý nghỉ phép
- [ ] Export to Excel/PDF
- [ ] Advanced reporting
- [ ] Email notifications
- [ ] File upload (avatar, documents)
- [ ] Audit logs
- [ ] Unit tests & Integration tests
- [ ] Switch to persistent database (MySQL, PostgreSQL)
- [ ] REST API với Spring Boot
- [ ] Docker deployment

## Troubleshooting

### Port đã được sử dụng

Thay đổi port trong plugin Jetty trong `pom.xml`:

```xml
<httpConnector>
    <port>8081</port> <!-- Thay đổi port -->
</httpConnector>
```

### Lỗi build

Kiểm tra Java version:

```bash
java -version  # Should be 17+
mvn -version   # Should be 3.6+
```

### Database không khởi tạo

Kiểm tra logs trong thư mục `logs/` để xem lỗi chi tiết.

## Tác giả

Dự án được tạo bởi AI Assistant cho mục đích học tập và demo.

## License

MIT License - Free to use for learning and commercial purposes.

## Support

Nếu có vấn đề hoặc câu hỏi, vui lòng tạo issue hoặc liên hệ.

---

**Happy Coding!** 🚀

