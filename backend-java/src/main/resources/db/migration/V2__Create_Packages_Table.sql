-- V2__Create_Packages_Table.sql

CREATE TABLE packages (
    id BIGSERIAL PRIMARY KEY,
    tracking_id VARCHAR(50) NOT NULL UNIQUE,
    buffer VARCHAR(50) NOT NULL, -- Ex: RTS, EHA
    rua VARCHAR(50) NOT NULL,    -- Ex: RTS-01
    profile_type VARCHAR(10) NOT NULL DEFAULT 'N/A', -- 'P', 'M', 'G'
    profile_value INTEGER NOT NULL DEFAULT 0,
    entry_timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE
);

-- Índices para performance (Diferencial de Sênior)
CREATE INDEX idx_packages_buffer ON packages(buffer);
CREATE INDEX idx_packages_tracking_id ON packages(tracking_id);