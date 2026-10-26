-- Usuarios do sistema (validados pelo POST /login)
INSERT INTO usuarios (username, senha) VALUES ('joao', '123');
INSERT INTO usuarios (username, senha) VALUES ('maria', '456');
INSERT INTO usuarios (username, senha) VALUES ('pedro', '789');

INSERT INTO login_attempts (username) VALUES ('joao');
INSERT INTO login_attempts (username) VALUES ('maria');
INSERT INTO login_attempts (username) VALUES ('pedro');

-- Usuarios com roles (JdbcUserDetailsManager / HTTP Basic)
INSERT INTO users (username, password, enabled) VALUES ('admin', '{noop}admin', TRUE);
INSERT INTO users (username, password, enabled) VALUES ('manutencao', '{noop}manutencao', TRUE);

INSERT INTO authorities (username, authority) VALUES ('admin', 'ROLE_ADMIN');
INSERT INTO authorities (username, authority) VALUES ('manutencao', 'ROLE_MANUTENCAO');
