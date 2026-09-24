package com.desafio.cartoes.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Produto do catálogo (o que o banco vende). Valores monetários sempre com 2 casas decimais.
 */
public record ProdutoCartao(
        TipoCartao tipo,
        BigDecimal limiteDisponivel,
        BigDecimal rendaMinima,
        BigDecimal valorAnuidadeMensal) {

    public ProdutoCartao {
        Objects.requireNonNull(tipo, "tipo é obrigatório");
        limiteDisponivel = normalizar(limiteDisponivel);
        rendaMinima = normalizar(rendaMinima);
        valorAnuidadeMensal = normalizar(valorAnuidadeMensal);

        // Invariante do enunciado: cartão sem anuidade SEMPRE tem anuidade 0.00.
        if (tipo == TipoCartao.CARTAO_SEM_ANUIDADE && valorAnuidadeMensal.signum() != 0) {
            throw new IllegalArgumentException("CARTAO_SEM_ANUIDADE deve ter valor_anuidade_mensal 0.00");
        }
    }

    public boolean rendaSuficiente(BigDecimal rendaMensal) {
        return rendaMensal.compareTo(rendaMinima) >= 0;
    }

    public CartaoOfertado aprovar() {
        return new CartaoOfertado(tipo, valorAnuidadeMensal, limiteDisponivel, StatusCartao.APROVADO);
    }

    private static BigDecimal normalizar(BigDecimal valor) {
        return Objects.requireNonNull(valor, "valor monetário é obrigatório").setScale(2, RoundingMode.HALF_UP);
    }
}