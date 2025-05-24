-- Roles
CREATE TABLE roles (
    id SERIAL,
    name VARCHAR(50) NOT NULL UNIQUE,
    PRIMARY KEY (id)
);

-- Users
CREATE TABLE users (
    id SERIAL,
    full_name VARCHAR(100) NOT NULL,
    provider_id VARCHAR(100),
    email VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
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

-- UserRoles
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Department Roles
CREATE TABLE department_roles (
    id SERIAL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    PRIMARY KEY (id)
);

-- Departments
CREATE TABLE departments (
    id SERIAL,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    avatar_url VARCHAR(255),
    banner_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

-- User Department Role Mappings
CREATE TABLE user_department_roles (
    id SERIAL,
    user_id BIGINT NOT NULL,
    department_id BIGINT NOT NULL,
    department_role_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE CASCADE,
    FOREIGN KEY (department_role_id) REFERENCES department_roles(id) ON DELETE CASCADE
);

-- Event Types
CREATE TABLE event_types (
    id SERIAL,
    name VARCHAR(100) NOT NULL,
    PRIMARY KEY (id)
);

-- Locations
CREATE TABLE locations (
    id SERIAL,
    address VARCHAR(255),
    ward VARCHAR(100),
    district VARCHAR(100),
    city VARCHAR(100),
    PRIMARY KEY (id)
);

-- Platforms
CREATE TABLE platforms (
    id SERIAL,
    name VARCHAR(100) NOT NULL,
    url VARCHAR(255),
    PRIMARY KEY (id)
);


-- Survey Related Enums
CREATE TYPE survey_status_enum AS ENUM (
    'draft',
    'opened',
    'closed'
);

-- Surveys
CREATE TABLE surveys (
    id SERIAL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status survey_status_enum NOT NULL DEFAULT 'draft',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT check_survey_dates CHECK (
        start_time < end_time
        AND start_time >= created_at
    )
);

-- Target audience Enums
CREATE TYPE target_audience AS ENUM (
    'student',
    'lecturer',
    'both'
);

-- Event mode Enums
CREATE TYPE event_mode AS ENUM (
    'online',
    'offline',
    'hybrid'
);

-- Event status Enums
CREATE TYPE event_status AS ENUM (
    'draft',
    'published',
    'closed',
    'canceled',
    'completed',
    'deleted'
);

-- Events
CREATE TABLE events (
    id SERIAL,
    name VARCHAR(100) NOT NULL,
    department_id BIGINT NOT NULL,
    type_id BIGINT NOT NULL,
    audience target_audience NOT NULL DEFAULT 'both',
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
    status event_status NOT NULL DEFAULT 'draft',
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
    )
);

-- Event Capacity
CREATE TABLE event_capacity (
    event_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    capacity INT NOT NULL CHECK (capacity >= 0),
    PRIMARY KEY (event_id, role_id),
    FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Event Images
CREATE TABLE images (
    id SERIAL,
    event_id BIGINT,
    url VARCHAR(255),
    PRIMARY KEY (id),
    FOREIGN KEY (event_id) REFERENCES events(id)
);

-- Tags
CREATE TABLE tags (
    id SERIAL,
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
    'registered',
    'canceled',
    'attended',
    'absent'
);

-- Event Registrations
CREATE TABLE registrations (
    id SERIAL,
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
    FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE
);

-- Question Related Enums
CREATE TYPE question_type_enum AS ENUM (
    'text',
    'radio',
    'checkbox',
    'dropdown',
    'rating'
);

-- Survey Questions
CREATE TABLE questions (
    id SERIAL,
    survey_id BIGINT,
    type question_type_enum,
    is_required BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (id),
    FOREIGN KEY (survey_id) REFERENCES surveys(id) ON DELETE CASCADE
);

-- Question Options
CREATE TABLE options (
    id SERIAL,
    question_id BIGINT,
    text VARCHAR(255),
    order_num INT,
    PRIMARY KEY (id),
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
);

-- Survey Responses
CREATE TABLE responses (
    id SERIAL,
    survey_id BIGINT,
    registration_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (survey_id) REFERENCES surveys(id) ON DELETE CASCADE,
    FOREIGN KEY (registration_id) REFERENCES registrations(id) ON DELETE CASCADE
);

-- Survey Answers
CREATE TABLE answers (
    id SERIAL,
    response_id BIGINT,
    question_id BIGINT,
    option_id BIGINT,
    answer_text TEXT,
    PRIMARY KEY (id),
    FOREIGN KEY (response_id) REFERENCES responses(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    FOREIGN KEY (option_id) REFERENCES options(id) ON DELETE CASCADE
);

-- Database Indexes
-- The following indexes are created to optimize query performance for frequently accessed columns
CREATE INDEX idx_events_name ON events(name);
CREATE INDEX idx_events_start_time ON events(start_time);
CREATE INDEX idx_events_end_time ON events(end_time);
CREATE INDEX idx_events_status ON events(status);
CREATE INDEX idx_events_department_id ON events(department_id);
CREATE INDEX idx_registrations_user_event ON registrations(user_id, event_id);
CREATE INDEX idx_registrations_status ON registrations(status);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_tags_name ON tags(name);
CREATE INDEX idx_types_name ON event_types(name);



