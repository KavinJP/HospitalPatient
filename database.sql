-- =====================================================
-- Library Management System Database Script
-- Database: MySQL
-- =====================================================

CREATE DATABASE IF NOT EXISTS library_db;
USE library_db;

-- Drop table if exists to start fresh
DROP TABLE IF EXISTS book;

-- Create book table
CREATE TABLE book (
    book_id VARCHAR(50) PRIMARY KEY,
    book_title VARCHAR(150) NOT NULL,
    author_name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL
);

-- Insert initial sample books
INSERT INTO book (book_id, book_title, author_name, status) VALUES
('B101', 'Java: The Complete Reference', 'Herbert Schildt', 'Available'),
('B102', 'Clean Code', 'Robert C. Martin', 'Issued'),
('B103', 'Introduction to Algorithms', 'Thomas H. Cormen', 'Reserved'),
('B104', 'Design Patterns', 'Erich Gamma', 'Available'),
('B105', 'Operating System Concepts', 'Abraham Silberschatz', 'Lost');

-- Verify inserted data
SELECT * FROM book;
