package com.poo.siga;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

// Para rodar este teste localmente: necessário PostgreSQL rodando (docker compose up postgres)
// Em CI, usar perfil de teste com H2 ou pular com: mvn package -DskipTests
@SpringBootTest
@ActiveProfiles("test")
class SigaApplicationTests {

    @Test
    void contextLoads() {
        // Valida que o contexto Spring carrega sem erros
    }
}
