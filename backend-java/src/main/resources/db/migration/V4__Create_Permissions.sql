-- V4__Create_Permissions.sql

CREATE TABLE permissions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE role_permissions (
    role_id BIGINT NOT NULL REFERENCES roles(id),
    permission_id BIGINT NOT NULL REFERENCES permissions(id),
    PRIMARY KEY (role_id, permission_id)
);

-- Seed das Permissões (Igual ao Go)
INSERT INTO permissions (name, description) VALUES 
('MANAGE_FIFO', 'Pode realizar entradas e saídas'),
('VIEW_LOGS', 'Ver logs de auditoria'),
('VIEW_USERS', 'Ver lista de usuários'),
('CREATE_USER', 'Criar usuários'),
('EDIT_USER', 'Editar usuários'),
('RESET_PASSWORD', 'Resetar senhas'),
('MOVE_PACKAGE', 'Mover pacotes'),
('GENERATE_QR_CODES', 'Gerar etiquetas');

-- Seed das Ligações (Admin tem tudo)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p WHERE r.name = 'admin';

-- Seed das Ligações (Leader - Mesmas do Admin no seu código Go original)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p WHERE r.name = 'leader';

-- Seed das Ligações (Fifo - Apenas operacional)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p WHERE r.name = 'fifo' AND p.name IN ('MANAGE_FIFO', 'MOVE_PACKAGE');