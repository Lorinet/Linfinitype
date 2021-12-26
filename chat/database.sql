CREATE DATABASE linfinitype_chat;
USE linfinitype_chat;
CREATE TABLE `users` (
    username VARCHAR(255),
    password VARCHAR(255),
    email VARCHAR(255),
    fullname VARCHAR(255)
    );
CREATE TABLE `messages` (
    sender VARCHAR(255),
    recipient VARCHAR(255),
    message TEXT,
    time_created DATETIME,
    seen BOOLEAN
    );