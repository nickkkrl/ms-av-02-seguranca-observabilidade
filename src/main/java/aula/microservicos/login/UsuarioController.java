package aula.microservicos.login;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import aula.microservicos.login.Dtos.Mensagem;
import aula.microservicos.login.Dtos.TrocaSenhaRequest;
import aula.microservicos.login.Dtos.UsuarioRequest;
import aula.microservicos.login.Dtos.UsuarioResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Tag(name = "Usuarios", description = "Login, troca de senha, cadastro e desbloqueio de usuarios")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @PostMapping("/login")
    @Operation(summary = "Efetua login", description = "Publico. Valida usuario e senha na tabela usuarios. "
            + "Apos mais de uma falha o usuario e bloqueado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario e senha validos"),
            @ApiResponse(responseCode = "401", description = "Usuario e senha invalidos (ou usuario bloqueado)") })
    public ResponseEntity<Mensagem> login(@RequestBody UsuarioRequest req) {
        if (service.login(req.username(), req.senha())) {
            return ResponseEntity.ok(new Mensagem("login efetuado com sucesso"));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Mensagem("usuario ou senha invalidos"));
    }

    @PutMapping("/trocarsenha/{userid}")
    @Operation(summary = "Troca a senha do usuario", description = "Publico. Confere a senha atual antes de gravar a nova.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Senha alterada"),
            @ApiResponse(responseCode = "400", description = "Nova senha invalida (vazia ou com mais de 10 caracteres)"),
            @ApiResponse(responseCode = "401", description = "Senha atual invalida"),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado") })
    public ResponseEntity<Mensagem> trocarSenha(
            @Parameter(description = "username do usuario") @PathVariable String userid,
            @Valid @RequestBody TrocaSenhaRequest req) {
        service.trocarSenha(userid, req.atual(), req.nova());
        return ResponseEntity.ok(new Mensagem("senha alterada com sucesso"));
    }

    @PostMapping("/")
    @Operation(summary = "Cadastra novo usuario", description = "Requer ROLE_MANUTENCAO (HTTP Basic).",
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario criado"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos"),
            @ApiResponse(responseCode = "401", description = "Nao autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem a role ROLE_MANUTENCAO"),
            @ApiResponse(responseCode = "409", description = "Usuario ja existe") })
    public ResponseEntity<Mensagem> cadastrar(@Valid @RequestBody UsuarioRequest req) {
        service.cadastrar(req.username(), req.senha());
        return ResponseEntity.status(HttpStatus.CREATED).body(new Mensagem("usuario cadastrado"));
    }

    @GetMapping("/")
    @Operation(summary = "Lista todos os usuarios", description = "Requer ROLE_MANUTENCAO (HTTP Basic).",
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de usuarios"),
            @ApiResponse(responseCode = "401", description = "Nao autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem a role ROLE_MANUTENCAO") })
    public List<UsuarioResponse> listar() {
        return service.listar();
    }

    @PutMapping("/unlock/{userid}")
    @Operation(summary = "Desbloqueia usuario", description = "Requer ROLE_ADMIN (HTTP Basic).",
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario desbloqueado"),
            @ApiResponse(responseCode = "401", description = "Nao autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem a role ROLE_ADMIN"),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado") })
    public ResponseEntity<Mensagem> desbloquear(@Parameter(description = "username do usuario") @PathVariable String userid) {
        service.desbloquear(userid);
        return ResponseEntity.ok(new Mensagem("usuario desbloqueado"));
    }
}
