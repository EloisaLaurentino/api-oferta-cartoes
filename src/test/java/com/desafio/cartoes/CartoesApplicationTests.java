package com.desafio.cartoes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@DisplayName("Aplicação")
class CartoesApplicationTests {

    @Test
    @DisplayName("o contexto sobe com a configuração padrão (valida application.yml e o catálogo)")
    void contextLoads() {
    }
}