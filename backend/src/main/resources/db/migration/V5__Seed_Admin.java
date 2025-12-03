package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Migração Java para inserir o Admin de forma segura.
 * O Flyway detecta esta classe automaticamente pela nomenclatura V5__.
 */
public class V5__Seed_Admin extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        // 1. Gerar o Hash seguro usando a mesma lógica da aplicação
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String passwordHash = passwordEncoder.encode("admin");

        // 2. Buscar o ID do papel 'admin'
        // Usamos JDBC puro aqui porque o contexto do Spring pode não estar pronto
        int roleId = 0;
        try (Statement select = context.getConnection().createStatement()) {
            try (ResultSet rows = select.executeQuery("SELECT id FROM roles WHERE name = 'admin'")) {
                if (rows.next()) {
                    roleId = rows.getInt("id");
                } else {
                    throw new RuntimeException("Papel 'admin' não encontrado na migração V5.");
                }
            }
        }

        // 3. Inserir ou Atualizar o Usuário
        // Usamos PreparedStatement para evitar qualquer erro de sintaxe/encoding
        String sql = """
            INSERT INTO users (full_name, username, password_hash, sector, role_id, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            ON CONFLICT (username) DO UPDATE 
            SET password_hash = EXCLUDED.password_hash;
        """;

        try (PreparedStatement insert = context.getConnection().prepareStatement(sql)) {
            insert.setString(1, "Administrador do Sistema");
            insert.setString(2, "admin");
            insert.setString(3, passwordHash); // O Hash gerado pelo Java entra aqui
            insert.setString(4, "ADMINISTRAÇÃO");
            insert.setInt(5, roleId);
            
            insert.execute();
        }
    }
}