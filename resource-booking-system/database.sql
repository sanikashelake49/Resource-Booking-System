-- =========================================================
-- Resource Booking System - Database Setup Script
-- =========================================================
-- Hibernate (spring.jpa.hibernate.ddl-auto=update) will automatically
-- create/update the tables (users, resources, reservations) on startup.
-- You only need to run the CREATE DATABASE statement below.
-- =========================================================

CREATE DATABASE IF NOT EXISTS resource_booking_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE resource_booking_db;

-- =========================================================
-- Optional: sample queries to verify data after the app has
-- started once (Hibernate will have created the tables and
-- DataInitializer will have seeded the data).
-- =========================================================

-- View all users (passwords are BCrypt-hashed)
-- SELECT id, username, email, role, created_at FROM users;

-- View all resources
-- SELECT id, name, type, location, price, available FROM resources;

-- View all reservations with resource + user info
-- SELECT r.id, u.username, res.name AS resource_name, r.start_time, r.end_time, r.price, r.status
-- FROM reservations r
-- JOIN users u ON r.user_id = u.id
-- JOIN resources res ON r.resource_id = res.id;

-- =========================================================
-- If you prefer NOT to rely on Hibernate auto-DDL, here is the
-- equivalent manual schema (kept in sync with the JPA entities):
-- =========================================================

-- CREATE TABLE IF NOT EXISTS users (
--     id BIGINT AUTO_INCREMENT PRIMARY KEY,
--     username VARCHAR(50) NOT NULL UNIQUE,
--     email VARCHAR(100) NOT NULL UNIQUE,
--     password VARCHAR(255) NOT NULL,
--     role VARCHAR(20) NOT NULL,
--     created_at DATETIME NOT NULL
-- );
--
-- CREATE TABLE IF NOT EXISTS resources (
--     id BIGINT AUTO_INCREMENT PRIMARY KEY,
--     name VARCHAR(100) NOT NULL,
--     description VARCHAR(500),
--     type VARCHAR(50),
--     location VARCHAR(100),
--     price DECIMAL(10,2) NOT NULL,
--     available BOOLEAN NOT NULL DEFAULT TRUE,
--     created_at DATETIME NOT NULL
-- );
--
-- CREATE TABLE IF NOT EXISTS reservations (
--     id BIGINT AUTO_INCREMENT PRIMARY KEY,
--     resource_id BIGINT NOT NULL,
--     user_id BIGINT NOT NULL,
--     start_time DATETIME NOT NULL,
--     end_time DATETIME NOT NULL,
--     price DECIMAL(10,2) NOT NULL,
--     status VARCHAR(20) NOT NULL,
--     created_at DATETIME NOT NULL,
--     CONSTRAINT fk_reservation_resource FOREIGN KEY (resource_id) REFERENCES resources(id),
--     CONSTRAINT fk_reservation_user FOREIGN KEY (user_id) REFERENCES users(id)
-- );
