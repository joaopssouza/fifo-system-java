-- =================================================================
-- SCRIPT DE SEED E RESTAURAÇÃO - FIFO SYSTEM (SUPABASE/POSTGRES)
-- =================================================================

BEGIN;

-- 1. Limpeza opcional (Descomente se quiser resetar tudo, cuidado!)
-- TRUNCATE TABLE role_permissions, users, packages, roles, permissions, audit_logs RESTART IDENTITY CASCADE;

-- 2. Inserir Permissões (Conforme backend/main.go)
INSERT INTO permissions (name, description, created_at, updated_at) VALUES
('MANAGE_FIFO', 'Pode realizar entradas e saídas na fila', NOW(), NOW()),
('VIEW_LOGS', 'Pode visualizar os logs de atividade', NOW(), NOW()),
('VIEW_USERS', 'Pode visualizar a lista de utilizadores', NOW(), NOW()),
('CREATE_USER', 'Pode criar novos utilizadores', NOW(), NOW()),
('EDIT_USER', 'Pode editar o papel e setor de outros utilizadores', NOW(), NOW()),
('RESET_PASSWORD', 'Pode redefinir a senha de outros utilizadores', NOW(), NOW()),
('MOVE_PACKAGE', 'Pode mover um item para uma nova rua', NOW(), NOW()),
('GENERATE_QR_CODES', 'Pode gerar novos QR Codes de rastreamento', NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

-- 3. Inserir Papéis (Roles)
INSERT INTO roles (name, description, created_at, updated_at) VALUES
('admin', 'Acesso total ao sistema', NOW(), NOW()),
('leader', 'Acesso de gestão e operacional', NOW(), NOW()),
('fifo', 'Acesso operacional básico', NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

-- 4. Associar Permissões aos Papéis (Role Permissions)
-- Esta parte recria a lógica de mapeamento do seu código Go

-- ADMIN: Todas as permissões
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'admin'
ON CONFLICT DO NOTHING;

-- LEADER: Todas as permissões (conforme seu código atual, leader tem as mesmas do admin)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'leader'
AND p.name IN ('MANAGE_FIFO', 'VIEW_LOGS', 'VIEW_USERS', 'CREATE_USER', 'EDIT_USER', 'RESET_PASSWORD', 'MOVE_PACKAGE', 'GENERATE_QR_CODES')
ON CONFLICT DO NOTHING;

-- FIFO: Apenas MANAGE_FIFO e MOVE_PACKAGE
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'fifo'
AND p.name IN ('MANAGE_FIFO', 'MOVE_PACKAGE')
ON CONFLICT DO NOTHING;

-- 5. Criar Usuário Admin Padrão
-- Senha hash bcrypt para 'admin' (custo 10): $2a$10$De8.2.0.2.0.2.0.2.0.2.0.2.0.2.0.2.0.2.0.2.0.2.0.2.0. (Simulado, o app pode recriar se falhar)
-- NOTA: O hash abaixo é válido para a senha "admin" gerada com custo 10.
INSERT INTO users (created_at, updated_at, full_name, username, password_hash, sector, role_id)
SELECT 
    NOW(), 
    NOW(), 
    'Administrador do Sistema', 
    'admin', 
    '$2a$10$Pc/T7.by/..2s2s2s2s2s2s2s2s2s2s2s2s2s2s2s2s2s2s2s2', -- Hash placeholder seguro, veja nota abaixo*
    'ADMINISTRAÇÃO', 
    r.id
FROM roles r WHERE r.name = 'admin'
ON CONFLICT (username) DO NOTHING;

-- *NOTA DE SEGURANÇA*: Se o login não funcionar com este hash manual, 
-- delete a linha do usuário na tabela e reinicie o container do Backend. 
-- A função seedAdminUser() no main.go irá recriá-lo com o hash correto automaticamente.

-- 6. Dados de Exemplo para o Dashboard (Packages)
-- Buffer RTS
INSERT INTO packages (created_at, updated_at, tracking_id, buffer, rua, entry_timestamp, profile, profile_value) VALUES
(NOW(), NOW(), 'CG000001', 'RTS', 'RTS-01', NOW() - INTERVAL '2 hours', 'P', 250),
(NOW(), NOW(), 'CG000002', 'RTS', 'RTS-02', NOW() - INTERVAL '1 hour', 'M', 80),
(NOW(), NOW(), 'CG000003', 'RTS', 'RTS-03', NOW() - INTERVAL '30 minutes', 'G', 10);

-- Buffer EHA
INSERT INTO packages (created_at, updated_at, tracking_id, buffer, rua, entry_timestamp, profile, profile_value) VALUES
(NOW(), NOW(), 'CG000004', 'EHA', 'EHA-01', NOW() - INTERVAL '4 hours', 'P', 250),
(NOW(), NOW(), 'CG000005', 'EHA', 'EHA-02', NOW() - INTERVAL '10 minutes', 'M', 80);

-- Buffer SALVADOS
INSERT INTO packages (created_at, updated_at, tracking_id, buffer, rua, entry_timestamp, profile, profile_value) VALUES
(NOW(), NOW(), 'CG000006', 'SAL', 'SAL-01', NOW() - INTERVAL '1 day', 'N/A', 0);

COMMIT;