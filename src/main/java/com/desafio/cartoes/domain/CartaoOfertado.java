package com.desafio.cartoes.domain;

import java.math.BigDecimal;

public record CartaoOfertado(
        TipoCartao tipo,
        BigDecimal valorAnuidadeMensal,
        BigDecimal valorLimiteDisponivel,
        StatusCartao status) {
}