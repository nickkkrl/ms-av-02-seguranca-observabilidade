package aula.microservicos.login;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Objetos de entrada e saida dos end-points (JSON).
 */
public final class Dtos {

    private Dtos() {
    }

    /** Corpo do POST /login e do POST / : {"username": "", "senha": ""} */
    public record UsuarioRequest(
            @NotBlank String username,
            @NotBlank @Size(max = 10, message = "a senha deve ter no maximo 10 caracteres") String senha) {
    }

    /** Corpo do PUT /trocarsenha/{userid} : {"atual": "", "nova": ""} */
    public record TrocaSenhaRequest(
            @NotBlank String atual,
            @NotBlank @Size(max = 10, message = "a senha deve ter no maximo 10 caracteres") String nova) {
    }

    /** Usuario retornado pelo GET / (sem a senha) */
    public record UsuarioResponse(
            String username,
            int tentativasFalhas,
            boolean bloqueado,
            LocalDateTime ultimaFalha,
            LocalDateTime bloqueadoEm) {
    }

    /** Mensagem simples de retorno */
    public record Mensagem(String mensagem) {
    }
}
