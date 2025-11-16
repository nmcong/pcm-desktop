-- Employee Management System Database Schema

-- Drop tables if exist
DROP TABLE IF EXISTS employees;
DROP TABLE IF EXISTS departments;

-- Create departments table
CREATE TABLE departments
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create employees table
CREATE TABLE employees
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_code VARCHAR(20)  NOT NULL UNIQUE,
    first_name    VARCHAR(50)  NOT NULL,
    last_name     VARCHAR(50)  NOT NULL,
    email         VARCHAR(100) NOT NULL UNIQUE,
    phone         VARCHAR(20),
    date_of_birth DATE,
    hire_date     DATE         NOT NULL,
    salary        DECIMAL(12, 2),
    department_id BIGINT,
    position      VARCHAR(100),
    status        VARCHAR(20) DEFAULT 'ACTIVE',
    address       VARCHAR(255),
    city          VARCHAR(50),
    country       VARCHAR(50),
    created_at    TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (department_id) REFERENCES departments (id) ON DELETE SET NULL
);

-- Create indexes for better performance
CREATE INDEX idx_employee_code ON employees (employee_code);
CREATE INDEX idx_employee_email ON employees (email);
CREATE INDEX idx_employee_department ON employees (department_id);
CREATE INDEX idx_employee_status ON employees (status);
CREATE INDEX idx_department_name ON departments (name);

