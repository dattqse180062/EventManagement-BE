-- Roles
CREATE TABLE roles (
    id BIGSERIAL,
    name VARCHAR(50) NOT NULL UNIQUE,
    PRIMARY KEY (id)
);

-- Users
CREATE TABLE users (
    id BIGSERIAL,
    full_name VARCHAR(100) NOT NULL,
    provider_id VARCHAR(100),
    email VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

-- UserRoles
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- RefreshTokens
CREATE TABLE refresh_tokens (
    id BIGSERIAL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Department Roles
CREATE TABLE department_roles (
    id BIGSERIAL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    PRIMARY KEY (id)
);

-- Departments
CREATE TABLE departments (
    id BIGSERIAL,
    code VARCHAR(25) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(100),
    avatar_url VARCHAR(255),
    banner_url VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

-- User Department Role Mappings
CREATE TABLE user_department_roles (
    id BIGSERIAL,
    user_id BIGINT NOT NULL,
    department_id BIGINT NOT NULL,
    department_role_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE CASCADE,
    FOREIGN KEY (department_role_id) REFERENCES department_roles(id) ON DELETE CASCADE,
    UNIQUE(user_id, department_id, department_role_id)
);

-- Event Types
CREATE TABLE event_types (
    id BIGSERIAL,
    name VARCHAR(100) NOT NULL,
    PRIMARY KEY (id)
);

-- Locations
CREATE TABLE locations (
    id BIGSERIAL,
    address VARCHAR(255),
    ward VARCHAR(100),
    district VARCHAR(100),
    city VARCHAR(100),
    PRIMARY KEY (id)
);

-- Platforms
CREATE TABLE platforms (
    id BIGSERIAL,
    name VARCHAR(100) NOT NULL,
    url VARCHAR(255),
    PRIMARY KEY (id)
);

-- Survey Related Enums
CREATE TYPE survey_status_enum AS ENUM (
    'DRAFT',
    'OPENED',
    'CLOSED'
);

-- Surveys
CREATE TABLE surveys (
    id BIGSERIAL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status survey_status_enum NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT check_survey_dates CHECK (
        start_time < end_time
        AND start_time >= created_at
    )
);

-- Question Related Enums
CREATE TYPE question_type_enum AS ENUM (
    'TEXT',
    'RADIO',
    'CHECKBOX',
    'DROPDOWN',
    'RATING'
);

-- Survey Questions
CREATE TABLE questions (
    id BIGSERIAL,
    survey_id BIGINT NOT NULL,
    question TEXT NOT NULL,
    order_num INT,
    type question_type_enum,
    is_required BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (id),
    FOREIGN KEY (survey_id) REFERENCES surveys(id) ON DELETE CASCADE
);

-- Question Options
CREATE TABLE options (
    id BIGSERIAL,
    question_id BIGINT NOT NULL,
    text VARCHAR(255),
    order_num INT,
    PRIMARY KEY (id),
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
);

-- Target audience Enums
CREATE TYPE target_audience AS ENUM (
    'STUDENT',
    'LECTURER',
    'BOTH'
);

-- Event mode Enums
CREATE TYPE event_mode AS ENUM (
    'ONLINE',
    'OFFLINE',
    'HYBRID'
);

-- Event status Enums
CREATE TYPE event_status AS ENUM (
    'DRAFT',
    'PUBLISHED',
    'BLOCKED',
    'CLOSED',
    'CANCELED',
    'COMPLETED'
);

-- Events
CREATE TABLE events (
    id BIGSERIAL,
    name VARCHAR(100) NOT NULL,
    department_id BIGINT NOT NULL,
    type_id BIGINT NOT NULL,
    audience target_audience NOT NULL DEFAULT 'BOTH',
    location_id BIGINT,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    max_capacity INT CHECK (max_capacity > 0),
    registration_start TIMESTAMP NOT NULL,
    registration_end TIMESTAMP NOT NULL,
    poster_url VARCHAR(255),
    banner_url VARCHAR(255),
    description TEXT,
    survey_id BIGINT,
    mode event_mode NOT NULL,
    platform_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status event_status NOT NULL DEFAULT 'DRAFT',
    PRIMARY KEY (id),
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE CASCADE,
    FOREIGN KEY (type_id) REFERENCES event_types(id) ON DELETE RESTRICT,
    FOREIGN KEY (location_id) REFERENCES locations(id) ON DELETE SET NULL,
    FOREIGN KEY (survey_id) REFERENCES surveys(id) ON DELETE SET NULL,
    FOREIGN KEY (platform_id) REFERENCES platforms(id) ON DELETE SET NULL,
    CONSTRAINT check_event_dates CHECK (
        start_time < end_time 
        AND registration_start < registration_end
        AND registration_end <= start_time
    ),
    UNIQUE(location_id),
    UNIQUE(platform_id)
);

-- Event Capacity
CREATE TABLE event_capacity (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    capacity INT NOT NULL CHECK (capacity >= 0),
    FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    UNIQUE(event_id, role_id)
);

-- Event Images
CREATE TABLE images (
    id BIGSERIAL,
    event_id BIGINT,
    url VARCHAR(255),
    PRIMARY KEY (id),
    FOREIGN KEY (event_id) REFERENCES events(id)
);

-- Tags
CREATE TABLE tags (
    id BIGSERIAL,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN,
    PRIMARY KEY (id)
);

-- Event Tags
CREATE TABLE event_tags (
    event_id BIGINT,
    tag_id BIGINT,
    PRIMARY KEY (event_id, tag_id),
    FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
);

-- Registration Related Enums
CREATE TYPE registration_status AS ENUM (
    'REGISTERED',
    'CANCELED',
    'ATTENDED',
    'ABSENT'
);

-- Event Registrations
CREATE TABLE registrations (
    id BIGSERIAL,
    user_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    checkin_url VARCHAR(255),
    status registration_status NOT NULL,
    canceled_at TIMESTAMP,
    attended BOOLEAN DEFAULT FALSE,
    survey_done BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    UNIQUE(user_id, event_id)
);

-- Survey Responses
CREATE TABLE responses (
    id BIGSERIAL,
    survey_id BIGINT,
    registration_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (survey_id) REFERENCES surveys(id) ON DELETE CASCADE,
    FOREIGN KEY (registration_id) REFERENCES registrations(id) ON DELETE CASCADE,
    UNIQUE(survey_id, registration_id)
);

-- Survey Answers
CREATE TABLE answers (
    id BIGSERIAL,
    response_id BIGINT,
    question_id BIGINT,
    option_id BIGINT NULL,
    answer_text TEXT,
    PRIMARY KEY (id),
    FOREIGN KEY (response_id) REFERENCES responses(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    FOREIGN KEY (option_id) REFERENCES options(id) ON DELETE CASCADE,
    UNIQUE(response_id, question_id, option_id)
);

-- Staff Roles
CREATE TABLE staff_roles (
    id BIGSERIAL,
    staff_role_name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    PRIMARY KEY (id)
);

-- Event Staffs
CREATE TABLE event_staffs (
    id BIGSERIAL,
    event_id BIGINT NOT NULL,
    staff_id BIGINT NOT NULL,
    staff_role_id INT NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    FOREIGN KEY (staff_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (staff_role_id) REFERENCES staff_roles(id) ON DELETE RESTRICT,
    UNIQUE(event_id, staff_id, staff_role_id)
);

-- Categories
CREATE TABLE categories (
    id BIGSERIAL,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

-- Event Categories
CREATE TABLE event_categories (
    event_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    PRIMARY KEY (event_id, category_id),
    FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
);

-- Roles
INSERT INTO roles (name) VALUES
  ('ROLE_STUDENT'),
  ('ROLE_LECTURER'),
  ('ROLE_ADMIN');

-- Users
INSERT INTO users (email, full_name, provider_id) VALUES
  ('danhlagi01472@gmail.com', 'LECTURER', 'LECTURER-TEST-01'),
  ('tqdat410@gmail.com', 'ADMIN', 'ADMIN-TEST-01'),
  ('hoangthao2222@gmail.com', 'LECTURER', 'LECTURER-TEST-02'),
  ('tuanhuymai168h@gmail.com', 'ADMIN', 'ADMIN-TEST-02');

-- UserRoles
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'danhlagi01472@gmail.com' AND r.name = 'ROLE_LECTURER';

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'tqdat410@gmail.com' AND r.name = 'ROLE_ADMIN';

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'hoangthao2222@gmail.com' AND r.name = 'ROLE_LECTURER';

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'tuanhuymai168h@gmail.com' AND r.name = 'ROLE_ADMIN';

-- DepartmentRoles
INSERT INTO department_roles (name, description) VALUES
('HEAD', 'Head of Department'),
('DEPUTY_HEAD', 'Deputy Head of Department'),
('STAFF', 'Department Staff');

-- Departments
INSERT INTO departments (name, code, description, avatar_url, banner_url, is_active, created_at, updated_at) VALUES
('Phòng Đào tạo', 'DT01', 'Quản lý đào tạo sinh viên', 'https://placebear.com/200/300', 'https://placebear.com/200/300', TRUE, NOW(), NOW()),
('Phòng Công tác SV', 'CTSV01', 'Quản lý hoạt động sinh viên', 'https://placebear.com/200/300', 'https://placebear.com/200/300', TRUE, NOW(), NOW());

-- UserDepartmentRoles
INSERT INTO user_department_roles (user_id, department_id, department_role_id, created_at, updated_at) VALUES
(1, 1, 1, NOW(), NOW()),
(1, 2, 1, NOW(), NOW()),
(3, 1, 1, NOW(), NOW()),
(3, 2, 1, NOW(), NOW());

-- EventTypes
INSERT INTO event_types (name) VALUES
('Seminar'),
('Workshop'),
('Conference'),
('Webinar'),
('Orientation'),
('Guest Lecture'),
('Networking Event'),
('Career Fair');

-- Locations
INSERT INTO locations (address, ward, district, city) VALUES
('Hội trường D1 trường đại học FPT', 'D1', 'TP.Thủ Đức', 'Hồ Chí Minh'),
('Hội trường Nhà hát lớn Nhà văn hóa Sinh viên', 'D1', 'TP.Thủ Đức', 'Hồ Chí Minh');

-- Platforms
INSERT INTO platforms (name, url) VALUES
('Zoom', 'https://zoom.us'),
('Google Meet', 'https://meet.google.com');

-- Events
INSERT INTO events (name, department_id, type_id, audience, location_id, start_time, end_time, max_capacity, registration_start, registration_end, poster_url, banner_url, description, survey_id, mode, platform_id, created_at, updated_at, status) VALUES
('Hội thảo Công nghệ', 1, 1, 'BOTH', 1, 
    NOW() + INTERVAL '30 days' + INTERVAL '9 hours',
    NOW() + INTERVAL '30 days' + INTERVAL '12 hours',
    100, 
    NOW() + INTERVAL '15 days',
    NOW() + INTERVAL '29 days',
    'https://placebear.com/200/300', 'https://placebear.com/200/300', 'Hội thảo về công nghệ mới', NULL, 'HYBRID', NULL, NOW(), NOW(), 'PUBLISHED'),
('Workshop Kỹ năng', 2, 2, 'BOTH', 2, 
    NOW() + INTERVAL '60 days' + INTERVAL '14 hours',
    NOW() + INTERVAL '60 days' + INTERVAL '17 hours',
    50, 
    NOW() + INTERVAL '45 days',
    NOW() + INTERVAL '59 days',
    'https://placebear.com/200/300', 'https://placebear.com/200/300', 'Workshop kỹ năng mềm', NULL, 'HYBRID', 1, NOW(), NOW(), 'PUBLISHED');

-- EventCapacity
INSERT INTO event_capacity (event_id, role_id, capacity) VALUES
(1, 2, 20),
(1, 3, 80),
(2, 2, 20),
(2, 3, 30);

-- Images
INSERT INTO images (event_id, url) VALUES
(1, 'https://placebear.com/200/300'),
(1, 'https://placebear.com/200/300'),
(2, 'https://placebear.com/200/300');

-- Tags
INSERT INTO tags (name, description, is_active, created_at, updated_at) VALUES
('Technology', 'Technology related events', TRUE, NOW(), NOW()),
('Professional Development', 'Career and skill development events', TRUE, NOW(), NOW()),
('Education', 'Educational events and workshops', TRUE, NOW(), NOW());

-- EventTags
INSERT INTO event_tags (event_id, tag_id) VALUES
(1, 1),  -- Hội thảo Công nghệ - Technology
(1, 2),  -- Hội thảo Công nghệ - Professional Development
(2, 1);  -- Workshop Kỹ năng - Technology

-- Registrations
INSERT INTO registrations (user_id, event_id, checkin_url, status, canceled_at, attended, survey_done, created_at, updated_at) VALUES
(1, 1, 'checkin1', 'REGISTERED', NULL, TRUE, TRUE, NOW(), NOW()),  -- LECTURER registered for Hội thảo Công nghệ
(2, 2, 'checkin2', 'REGISTERED', NULL, TRUE, TRUE, NOW(), NOW());  -- ADMIN registered for Workshop Kỹ năng

-- Surveys
INSERT INTO surveys (title, description, start_time, end_time, status, created_at, updated_at) VALUES
('Khảo sát Hội thảo Công nghệ', 'Đánh giá hội thảo công nghệ', 
    NOW() + INTERVAL '30 days', 
    NOW() + INTERVAL '31 days', 
    'OPENED', NOW(), NOW()),
('Khảo sát Workshop Kỹ năng', 'Đánh giá workshop kỹ năng', 
    NOW() + INTERVAL '60 days', 
    NOW() + INTERVAL '61 days', 
    'OPENED', NOW(), NOW());

-- Update events with survey_id
UPDATE events e
SET survey_id = (
    SELECT s.id 
    FROM surveys s 
    WHERE (e.name = 'Hội thảo Công nghệ' AND s.title = 'Khảo sát Hội thảo Công nghệ')
       OR (e.name = 'Workshop Kỹ năng' AND s.title = 'Khảo sát Workshop Kỹ năng')
    LIMIT 1
)
WHERE e.name IN ('Hội thảo Công nghệ', 'Workshop Kỹ năng');

-- Questions
INSERT INTO questions (survey_id, question, order_num, type, is_required) VALUES
(1, 'Bạn đánh giá trải nghiệm của mình như thế nào?', 1, 'TEXT', TRUE),
(1, 'Bạn đã từng sử dụng dịch vụ của chúng tôi trước đây chưa?', 2, 'RADIO', FALSE),
(2, 'Bạn có sẵn sàng giới thiệu dịch vụ cho bạn bè không?', 1, 'RADIO', TRUE);

-- Options
INSERT INTO options (question_id, text, order_num) VALUES
(2, 'Rất tốt', 1),
(2, 'Bình thường', 2),
(2, 'Chưa tốt', 3),
(3, 'Hài lòng', 1),
(3, 'Không hài lòng', 2);

-- Responses (after users have attended and completed surveys)
INSERT INTO responses (survey_id, registration_id, created_at, updated_at) VALUES
(1, (SELECT id FROM registrations WHERE user_id = 1 AND event_id = 1), NOW(), NOW()),  -- LECTURER's response to Hội thảo Công nghệ survey
(2, (SELECT id FROM registrations WHERE user_id = 2 AND event_id = 2), NOW(), NOW());  -- ADMIN's response to Workshop Kỹ năng survey

-- Answers
INSERT INTO answers (response_id, question_id, option_id, answer_text) VALUES
(
    (SELECT r.id FROM responses r 
     JOIN registrations reg ON r.registration_id = reg.id 
     WHERE reg.user_id = 1 AND reg.event_id = 1),
    2, 1, 'Rất tốt'
),
(
    (SELECT r.id FROM responses r 
     JOIN registrations reg ON r.registration_id = reg.id 
     WHERE reg.user_id = 2 AND reg.event_id = 2),
    3, 4, 'Hài lòng'
);

-- Staff Roles
INSERT INTO staff_roles (staff_role_name, description) VALUES
('EVENT_MANAGER', 'Manager of the event'),
('EVENT_CHECKIN', 'Staff check-in for the event'),
('EVENT_STAFF', 'Staff member for the event');

-- Event Staffs
INSERT INTO event_staffs (event_id, staff_id, staff_role_id, assigned_at, updated_at) VALUES
(1, 1, 1, NOW(), NOW()),  -- Event 1, Staff 1, Role 1 (EVENT_MANAGER)
(1, 2, 2, NOW(), NOW()),  -- Event 1, Staff 2, Role 2 (EVENT_CHECKIN)
(2, 3, 1, NOW(), NOW()),  -- Event 2, Staff 3, Role 1 (EVENT_MANAGER)
(2, 4, 2, NOW(), NOW());  -- Event 2, Staff 4, Role 2 (EVENT_CHECKIN)

-- Categories
INSERT INTO categories (code, name, description, is_active, created_at, updated_at) VALUES
('HOT', 'Technology', 'Events related to technology', TRUE, NOW(), NOW()),
('UPCOMING', 'Soft Skills', 'Events for developing soft skills', TRUE, NOW(), NOW()),
('TRENDING', 'Career Development', 'Events focused on career growth', TRUE, NOW(), NOW());

-- Event Categories
INSERT INTO event_categories (event_id, category_id) VALUES
(1, 1),
(2, 2);