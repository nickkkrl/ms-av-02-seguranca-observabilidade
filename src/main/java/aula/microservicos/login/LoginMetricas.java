package aula.microservicos.login;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * Indicadores expostos em /actuator/prometheus:
 * - login_validos_total
 * - login_invalidos_total
 * - usuarios_bloqueados
 */
@Component
public class LoginMetricas {

    private final Counter loginsValidos;
    private final Counter loginsInvalidos;

    public LoginMetricas(MeterRegistry registry, UsuarioRepository repository) {
        loginsValidos = Counter.builder("login.validos")
                .description("Quantidade de logins validos")
                .register(registry);

        loginsInvalidos = Counter.builder("login.invalidos")
                .description("Quantidade de logins invalidos")
                .register(registry);

        // Gauge consulta o banco a cada coleta do Prometheus
        Gauge.builder("usuarios.bloqueados", repository, UsuarioRepository::totalBloqueados)
                .description("Total de usuarios bloqueados")
                .register(registry);
    }

    public void loginValido() {
        loginsValidos.increment();
    }

    public void loginInvalido() {
        loginsInvalidos.increment();
    }
}
