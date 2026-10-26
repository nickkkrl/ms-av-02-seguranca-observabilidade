package aula.microservicos.login;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import aula.microservicos.login.Dtos.UsuarioResponse;

@Service
public class UsuarioService {

    /** "Caso o usuario falhe mais de uma vez no login ele deve ser bloqueado" */
    private static final int MAX_FALHAS_PERMITIDAS = 1;

    private final UsuarioRepository repository;
    private final LoginMetricas metricas;

    public UsuarioService(UsuarioRepository repository, LoginMetricas metricas) {
        this.repository = repository;
        this.metricas = metricas;
    }

    /** @return true se usuario e senha forem validos (e o usuario nao estiver bloqueado) */
    @Transactional
    public boolean login(String username, String senha) {
        var senhaBanco = repository.buscarSenha(username);

        // usuario inexistente
        if (senhaBanco.isEmpty()) {
            metricas.loginInvalido();
            return false;
        }

        // usuario bloqueado nao consegue logar, mesmo com a senha correta
        if (repository.estaBloqueado(username)) {
            metricas.loginInvalido();
            return false;
        }

        if (senhaBanco.get().equals(senha)) {
            repository.zerarFalhas(username);
            metricas.loginValido();
            return true;
        }

        int falhas = repository.registrarFalha(username);
        if (falhas > MAX_FALHAS_PERMITIDAS) {
            repository.bloquear(username);
        }
        metricas.loginInvalido();
        return false;
    }

    @Transactional
    public void trocarSenha(String username, String atual, String nova) {
        var senhaBanco = repository.buscarSenha(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "usuario nao encontrado"));

        if (!senhaBanco.equals(atual)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "senha atual invalida");
        }
        repository.atualizarSenha(username, nova);
    }

    @Transactional
    public void cadastrar(String username, String senha) {
        if (repository.existe(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "usuario ja cadastrado");
        }
        repository.inserir(username, senha);
    }

    @Transactional
    public void desbloquear(String username) {
        if (!repository.existe(username)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "usuario nao encontrado");
        }
        repository.desbloquear(username);
    }

    public List<UsuarioResponse> listar() {
        return repository.listar();
    }
}
