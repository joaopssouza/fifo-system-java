-- V1__Initial_Schema.sql

-- Criação da tabela de Papéis (Roles)
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Criação da tabela de Usuários (Users)
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    sector VARCHAR(50) NOT NULL DEFAULT 'Geral',
    role_id BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE,
    
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- Inserção dos Papéis Padrão (Seed inicial obrigatória para o sistema funcionar)
INSERT INTO roles (name, description) VALUES 
('admin', 'Administrador do Sistema - Acesso Total'),
('leader', 'Liderança - Gestão e Operação'),
('fifo', 'Operador - Acesso Básico')
ON CONFLICT (name) DO NOTHING;