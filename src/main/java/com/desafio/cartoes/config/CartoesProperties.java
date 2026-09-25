package com.desafio.cartoes.config;

import com.desafio.cartoes.domain.TipoCartao;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.List;

/**
 * Parâmetros de negócio externalizados (application.yml / variáveis de ambiente).
 * Validados na subida: configuração inválida derruba a aplicação (fail-fast).
 */
@Validated
@ConfigurationProperties(prefix = "cartoes")
public record CartoesProperties(
        @PositiveOrZero int idadeMinima,
        @NotEmpty @Valid List<ProdutoProperties> produtos) {

    public record ProdutoProperties(
            @NotNull TipoCartao tipo,
            @NotNull @PositiveOrZero BigDecimal limiteDisponivel,
            @NotNull @PositiveOrZero BigDecimal rendaMinima,
            @NotNull @PositiveOrZero BigDecimal valorAnuidadeMensal) {
    }
}