package aula.microservicos.login;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import aula.microservicos.login.Dtos.UsuarioResponse;

/**
 * Acesso as tabelas "usuarios" e "login_attempts" via JdbcTemplate.
 */
@Repository
public class UsuarioRepository {

    private final JdbcTemplate jdbc;

    public UsuarioRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ---------- usuarios ----------

    public Optional<String> buscarSenha(String username) {
        List<String> senhas = jdbc.queryForList(
                "SELECT senha FROM usuarios WHERE username = ?", String.class, username);
        return senhas.stream().findFirst();
    }

    public boolean existe(String username) {
        Integer qtd = jdbc.queryForObject(
                "SELECT COUNT(*) FROM usuarios WHERE username = ?", Integer.class, username);
        return qtd != null && qtd > 0;
    }

    public void inserir(String username, String senha) {
        jdbc.update("INSERT INTO usuarios (username, senha) VALUES (?, ?)", username, senha);
        jdbc.update("INSERT INTO login_attempts (username) VALUES (?)", username);
    }

    public void atualizarSenha(String username, String novaSenha) {
        jdbc.update("UPDATE usuarios SET senha = ? WHERE username = ?", novaSenha, username);
    }

    public List<UsuarioResponse> listar() {
        return jdbc.query("""
                SELECT u.username, la.failed_attempts, la.locked, la.last_failed_at, la.locked_at
                  FROM usuarios u
                  LEFT JOIN login_attempts la ON la.username = u.username
                 ORDER BY u.username
                """,
                (rs, i) -> new UsuarioResponse(
                        rs.getString("username"),
                        rs.getInt("failed_attempts"),
                        rs.getBoolean("locked"),
                        toLocal(rs.getTimestamp("last_failed_at")),
                        toLocal(rs.getTimestamp("locked_at"))));
    }

    // ---------- login_attempts ----------

    public boolean estaBloqueado(String username) {
        List<Boolean> r = jdbc.queryForList(
                "SELECT locked FROM login_attempts WHERE username = ?", Boolean.class, username);
        return !r.isEmpty() && Boolean.TRUE.equals(r.get(0));
    }

    /** Soma uma falha e retorna o total de falhas atual. */
    public int registrarFalha(String username) {
        garantirRegistroTentativas(username);
        jdbc.update("""
                UPDATE login_attempts
                   SET failed_attempts = failed_attempts + 1,
                       last_failed_at = CURRENT_TIMESTAMP,
                       updated_at = CURRENT_TIMESTAMP
                 WHERE username = ?
                """, username);
        return jdbc.queryForObject(
                "SELECT failed_attempts FROM login_attempts WHERE username = ?", Integer.class, username);
    }

    public void bloquear(String username) {
        jdbc.update("""
                UPDATE login_attempts
                   SET locked = TRUE, locked_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP
                 WHERE username = ?
                """, username);
    }

    /** Zera as falhas (login valido). */
    public void zerarFalhas(String username) {
        garantirRegistroTentativas(username);
        jdbc.update("""
                UPDATE login_attempts
                   SET failed_attempts = 0, updated_at = CURRENT_TIMESTAMP
                 WHERE username = ?
                """, username);
    }

    /** Desbloqueia e zera as falhas. */
    public void desbloquear(String username) {
        garantirRegistroTentativas(username);
        jdbc.update("""
                UPDATE login_attempts
                   SET locked = FALSE, failed_attempts = 0, locked_at = NULL, updated_at = CURRENT_TIMESTAMP
                 WHERE username = ?
                """, username);
    }

    public int totalBloqueados() {
        Integer qtd = jdbc.queryForObject(
                "SELECT COUNT(*) FROM login_attempts WHERE locked = TRUE", Integer.class);
        return qtd == null ? 0 : qtd;
    }

    private void garantirRegistroTentativas(String username) {
        jdbc.update("MERGE INTO login_attempts (username) KEY (username) VALUES (?)", username);
    }

    private static LocalDateTime toLocal(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }
}
