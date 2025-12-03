-- V3__Create_Audit_Logs_Table.sql

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    user_fullname VARCHAR(100),
    action VARCHAR(50) NOT NULL, -- ENTRADA, SAIDA, MOVIMENTACAO
    details TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indexar por data e utilizador é essencial para logs (Performance)
CREATE INDEX IF NOT EXISTS idx_audit_logs_created_at ON audit_logs(created_at);
CREATE INDEX IF NOT EXISTS idx_audit_logs_username ON audit_logs(username);