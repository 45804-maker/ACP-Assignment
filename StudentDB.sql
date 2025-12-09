CREATE DATABASE IF NOT EXISTS StudentDB;
USE StudentDB;

CREATE TABLE students (
    id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    age INT,
    email VARCHAR(100)
);
