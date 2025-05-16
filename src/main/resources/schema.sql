CREATE TABLE users (
  id BIGSERIAL PRIMARY KEY,
  email VARCHAR(255) NOT NULL UNIQUE,
  full_name VARCHAR(255),
  provider_user_id VARCHAR(255) NOT NULL UNIQUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE roles (
  id SERIAL PRIMARY KEY,
  name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE user_roles (
  user_id BIGINT NOT NULL,
  role_id INT NOT NULL,
  PRIMARY KEY (user_id, role_id),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

CREATE TABLE refresh_tokens (
  id BIGSERIAL PRIMARY KEY,
  token VARCHAR(255) NOT NULL UNIQUE,
  expiry_date TIMESTAMP NOT NULL,
  issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  user_id BIGINT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

INSERT INTO roles (name) VALUES
  ('ROLE_STUDENT'),
  ('ROLE_LECTURER'),
  ('ROLE_ADMIN');

INSERT INTO users (email, full_name, provider_user_id) VALUES
  ('danhlagi0472@gmail.com', 'LECTURER', 'LECTURER-TEST-01'),
  ('tqdat410@gmail.com', 'ADMIN', 'ADMIN-TEST-01');

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'danhlagi0472@gmail.com' AND r.name = 'ROLE_LECTURER';

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'tqdat410@gmail.com' AND r.name = 'ROLE_ADMIN';