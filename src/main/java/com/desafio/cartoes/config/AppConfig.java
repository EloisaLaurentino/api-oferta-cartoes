package com.desafio.cartoes.config;

import com.desafio.cartoes.domain.AvaliadorElegibilidade;
import com.desafio.cartoes.domain.CatalogoProdutos;
import com.desafio.cartoes.domain.ProdutoCartao;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Único lugar que "cola" o domínio (Java puro) ao Spring.
 */
@Configuration
public class AppConfig {

    /**
     * Relógio injetável: permite testar regras dependentes de data (idade) sem depender de "hoje".
     * O fuso é explícito para o cálculo de idade não variar conforme o fuso do container (geralmente UTC).
     */
    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("America/Sao_Paulo"));
    }

    @Bean
    public CatalogoProdutos catalogoProdutos(CartoesProperties propriedades) {
        var produtos = propriedades.produtos().stream()
                .map(p -> new ProdutoCartao(p.tipo(), p.limiteDisponivel(), p.rendaMinima(), p.valorAnuidadeMensal()))
                .toList();
        return new CatalogoProdutos(produtos);
    }

    @Bean
    public AvaliadorElegibilidade avaliadorElegibilidade(CatalogoProdutos catalogo) {
        return AvaliadorElegibilidade.padrao(catalogo);
    }
}