-- Oracle 23c Schema Migration Script
-- This script recreates the MSSQL schema in Oracle using modern 23c features.

-- Create asset_sensor_readings table
CREATE TABLE IF NOT EXISTS asset_sensor_readings (
    readingid NUMBER GENERATED ALWAYS AS IDENTITY,
    assetid VARCHAR2(50) NOT NULL,
    reading_value FLOAT NOT NULL,
    sensor_type VARCHAR2(30) NOT NULL,
    status VARCHAR2(20),
    timestamp TIMESTAMP(6),
    uom VARCHAR2(10),
    CONSTRAINT pk_asset_readings PRIMARY KEY (readingid)
);

-- Performance indexes for sensor readings
CREATE INDEX IF NOT EXISTS idx_asset_readings_asset ON asset_sensor_readings(assetid);
CREATE INDEX IF NOT EXISTS idx_asset_readings_time ON asset_sensor_readings(timestamp);

-- Create app_users table
CREATE TABLE IF NOT EXISTS app_users (
    id NUMBER(19) GENERATED ALWAYS AS IDENTITY,
    username NVARCHAR2(255) NOT NULL,
    password NVARCHAR2(255) NOT NULL,
    CONSTRAINT pk_app_users PRIMARY KEY (id),
    CONSTRAINT unq_app_users_username UNIQUE (username)
);

-- Create user_roles table
CREATE TABLE IF NOT EXISTS user_roles (
    user_id NUMBER(19) NOT NULL,
    role_name NVARCHAR2(100),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES app_users(id)
);

-- Create audit_logs table (using modern Oracle features)
CREATE TABLE IF NOT EXISTS audit_logs (
    id NUMBER(19) GENERATED ALWAYS AS IDENTITY,
    timestamp TIMESTAMP(6) NOT NULL,
    event NVARCHAR2(100) NOT NULL,
    username NVARCHAR2(255),
    path NVARCHAR2(2000),
    reason NCLOB,
    CONSTRAINT pk_audit_logs PRIMARY KEY (id)
);

-- Password reset tokens (OTP Handshake)
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id NUMBER(19) GENERATED ALWAYS AS IDENTITY,
    user_id NUMBER(19) NOT NULL,
    token VARCHAR2(255) NOT NULL,
    expiry TIMESTAMP(6) NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    CONSTRAINT pk_prt PRIMARY KEY (id),
    CONSTRAINT fk_prt_user FOREIGN KEY (user_id) REFERENCES app_users(id)
);

CREATE INDEX IF NOT EXISTS idx_prt_token ON password_reset_tokens(token);

-- 1. Insert the user (The ID is generated automatically)
INSERT INTO app_users (username, password) 
VALUES ('your_username', 'YOUR_BCRYPT_HASH_HERE');

-- 2. Get the ID for your user
SELECT id FROM app_users WHERE username = 'your_username';

-- 3. Map the role (Replace <USER_ID> with the result from step 2)
INSERT INTO user_roles (user_id, role_name) 
VALUES (<USER_ID>, 'ROLE_GATEWAY_ADMIN');
