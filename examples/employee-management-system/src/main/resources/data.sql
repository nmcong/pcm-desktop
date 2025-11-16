-- Sample data for Employee Management System

-- Insert departments
INSERT INTO departments (name, description)
VALUES ('Kỹ thuật', 'Phòng phát triển phần mềm và hệ thống'),
       ('Nhân sự', 'Phòng quản lý nhân sự và tuyển dụng'),
       ('Kế toán', 'Phòng tài chính và kế toán'),
       ('Marketing', 'Phòng marketing và truyền thông'),
       ('Kinh doanh', 'Phòng kinh doanh và phát triển thị trường'),
       ('Hành chính', 'Phòng hành chính tổng hợp');

-- Insert sample employees
INSERT INTO employees (employee_code, first_name, last_name, email, phone, date_of_birth, hire_date, salary,
                       department_id, position, status, address, city, country)
VALUES ('EMP001', 'Nguyễn', 'Văn An', 'nguyen.van.an@company.com', '0901234567', '1990-05-15', '2020-01-10',
        20000000.00, 1, 'Senior Developer', 'ACTIVE', '123 Nguyễn Huệ', 'Hồ Chí Minh', 'Việt Nam'),
       ('EMP002', 'Trần', 'Thị Bình', 'tran.thi.binh@company.com', '0902345678', '1992-08-20', '2020-03-15',
        15000000.00, 1, 'Developer', 'ACTIVE', '456 Lê Lợi', 'Hồ Chí Minh', 'Việt Nam'),
       ('EMP003', 'Lê', 'Văn Cường', 'le.van.cuong@company.com', '0903456789', '1988-12-10', '2019-06-01', 25000000.00,
        1, 'Tech Lead', 'ACTIVE', '789 Hai Bà Trưng', 'Hà Nội', 'Việt Nam'),
       ('EMP004', 'Phạm', 'Thị Dung', 'pham.thi.dung@company.com', '0904567890', '1995-03-25', '2021-02-20',
        12000000.00, 2, 'HR Specialist', 'ACTIVE', '321 Trần Hưng Đạo', 'Hà Nội', 'Việt Nam'),
       ('EMP005', 'Hoàng', 'Văn Em', 'hoang.van.em@company.com', '0905678901', '1993-07-30', '2021-05-10', 18000000.00,
        3, 'Accountant', 'ACTIVE', '654 Võ Văn Tần', 'Đà Nẵng', 'Việt Nam'),
       ('EMP006', 'Võ', 'Thị Fay', 'vo.thi.fay@company.com', '0906789012', '1991-11-05', '2020-09-15', 16000000.00, 4,
        'Marketing Manager', 'ACTIVE', '987 Lý Thường Kiệt', 'Hồ Chí Minh', 'Việt Nam'),
       ('EMP007', 'Đặng', 'Văn Giang', 'dang.van.giang@company.com', '0907890123', '1994-04-18', '2021-08-01',
        22000000.00, 5, 'Sales Manager', 'ACTIVE', '147 Ngô Quyền', 'Hà Nội', 'Việt Nam'),
       ('EMP008', 'Bùi', 'Thị Hà', 'bui.thi.ha@company.com', '0908901234', '1996-09-22', '2022-01-05', 10000000.00, 6,
        'Administrative Assistant', 'ACTIVE', '258 Điện Biên Phủ', 'Đà Nẵng', 'Việt Nam'),
       ('EMP009', 'Ngô', 'Văn Ích', 'ngo.van.ich@company.com', '0909012345', '1989-06-14', '2019-11-20', 28000000.00, 1,
        'Engineering Manager', 'ACTIVE', '369 Phan Châu Trinh', 'Hồ Chí Minh', 'Việt Nam'),
       ('EMP010', 'Lý', 'Thị Kim', 'ly.thi.kim@company.com', '0900123456', '1997-02-08', '2022-04-12', 9000000.00, 2,
        'HR Assistant', 'ACTIVE', '741 Tôn Đức Thắng', 'Hà Nội', 'Việt Nam');

