-- Initial data for Gallery Reservation System

-- Insert default users (password is: password123 for all)
INSERT INTO users (email, password, name, phone, organization, role, created_at, updated_at) VALUES
('admin@gallery.com', '$2a$10$xGJ9Q7K5X8M5H7Y3J5Z5K5O5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5', 'Admin User', '+1-555-0100', 'Gallery Administration', 'ADMIN', NOW(), NOW()),
('director1@gallery.com', '$2a$10$xGJ9Q7K5X8M5H7Y3J5Z5K5O5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5', 'John Director', '+1-555-0101', 'Gallery Direction', 'DIRECTOR_LEVEL_1', NOW(), NOW()),
('director2@gallery.com', '$2a$10$xGJ9Q7K5X8M5H7Y3J5Z5K5O5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5', 'Sarah Senior', '+1-555-0102', 'Gallery Direction', 'DIRECTOR_LEVEL_2', NOW(), NOW()),
('applicant@example.com', '$2a$10$xGJ9Q7K5X8M5H7Y3J5Z5K5O5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5Y5', 'Jane Applicant', '+1-555-0103', 'Art Foundation', 'APPLICANT', NOW(), NOW());

-- Insert sample rooms
INSERT INTO rooms (name, description, capacity, location, is_active, created_at, updated_at) VALUES
('Main Gallery', 'The primary exhibition space with natural lighting and high ceilings', 150, 'Building A - Floor 1', true, NOW(), NOW()),
('Small Gallery', 'Intimate space perfect for smaller exhibitions and workshops', 40, 'Building A - Floor 2', true, NOW(), NOW()),
('Conference Room', 'Professional meeting space with AV equipment', 30, 'Building B - Floor 1', true, NOW(), NOW()),
('Outdoor Courtyard', 'Open-air venue for events and receptions', 200, 'Building A - Courtyard', true, NOW(), NOW()),
('Studio Space', 'Flexible creative workspace for artists in residence', 20, 'Building C - Floor 1', true, NOW(), NOW());