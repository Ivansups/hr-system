CREATE TABLE departments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL
);

CREATE TABLE positions (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(20) NOT NULL,
    title VARCHAR(200) NOT NULL,
    base_salary NUMERIC(12, 2) NOT NULL
);

CREATE TABLE developer_positions (
    position_id BIGINT PRIMARY KEY REFERENCES positions (id) ON DELETE CASCADE,
    tech_stack VARCHAR(200) NOT NULL,
    grade INTEGER NOT NULL
);

CREATE TABLE manager_positions (
    position_id BIGINT PRIMARY KEY REFERENCES positions (id) ON DELETE CASCADE,
    team_size INTEGER NOT NULL
);

CREATE TABLE salesperson_positions (
    position_id BIGINT PRIMARY KEY REFERENCES positions (id) ON DELETE CASCADE,
    sales_percent NUMERIC(5, 2) NOT NULL
);

CREATE TABLE employees (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(200) NOT NULL,
    hire_date DATE NOT NULL,
    department_id BIGINT NOT NULL REFERENCES departments (id),
    position_id BIGINT NOT NULL REFERENCES positions (id)
);

CREATE INDEX idx_employees_department_id ON employees (department_id);
CREATE INDEX idx_employees_position_id ON employees (position_id);
