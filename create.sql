CREATE DATABASE IF NOT EXISTS asih;
USE asih;
CREATE TABLE items(itemID INT UNSIGNED PRIMARY KEY, name VARCHAR(300), sellerID VARCHAR(50), firstBid DECIMAL(8,2), currentBid DECIMAL(8,2), started DATETIME, ends DATETIME, description VARCHAR(4000));
CREATE TABLE bids(itemID INT UNSIGNED, bidderID VARCHAR(50), amount DECIMAL(8,2), bidtime DATETIME, primary key (itemID, amount));
CREATE TABLE categories(itemID INT UNSIGNED, category VARCHAR(200), PRIMARY KEY (itemID, category));
CREATE TABLE buyprice (itemID INT UNSIGNED PRIMARY KEY, price DECIMAL(8,2));
CREATE TABLE locations (userID VARCHAR(50) PRIMARY KEY, location VARCHAR(100));
CREATE TABLE countries (userID VARCHAR(50) PRIMARY KEY, country VARCHAR(50));
CREATE TABLE sellerrating (userID VARCHAR(50) PRIMARY KEY, srating INT);
CREATE TABLE buyerrating (userID VARCHAR(50) PRIMARY KEY, brating INT);
CREATE TABLE userlatlong (userID VARCHAR(50) PRIMARY KEY, latitude DECIMAL(10,7), longitude DECIMAL(10,7));