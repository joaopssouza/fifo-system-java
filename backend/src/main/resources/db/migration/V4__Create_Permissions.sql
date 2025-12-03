-- V4__Create_Permissions.sql

-- 1. Criação Segura das Tabelas
CREATE TABLE IF NOT EXISTS permissions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS role_permissions (
    role_id BIGINT NOT NULL REFERENCES roles(id),
    permission_id BIGINT NOT NULL REFERENCES permissions(id),
    PRIMARY KEY (role_id, permission_id)
);

-- 2. Inserção Segura das Permissões (Ignora se já existir)
INSERT INTO permissions (name, description) VALUES 
('MANAGE_FIFO', 'Pode realizar entradas e saídas'),
('VIEW_LOGS', 'Ver logs de auditoria'),
('VIEW_USERS', 'Ver lista de usuários'),
('CREATE_USER', 'Criar usuários'),
('EDIT_USER', 'Editar usuários'),
('RESET_PASSWORD', 'Resetar senhas'),
('MOVE_PACKAGE', 'Mover pacotes'),
('GENERATE_QR_CODES', 'Gerar etiquetas')
ON CONFLICT (name) DO NOTHING;

-- 3. Inserção Segura das Ligações (Role <-> Permission)

-- Admin: Todas as permissões
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'admin'
ON CONFLICT DO NOTHING;

-- Leader: Mesmas do Admin
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'leader'
ON CONFLICT DO NOTHING;

-- Fifo: Apenas operacional
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'fifo' AND p.name IN ('MANAGE_FIFO', 'MOVE_PACKAGE')
ON CONFLICT DO NOTHING;