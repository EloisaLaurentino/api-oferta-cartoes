package com.desafio.cartoes.support;

import com.desafio.cartoes.domain.CatalogoProdutos;
import com.desafio.cartoes.domain.ProdutoCartao;
import com.desafio.cartoes.domain.TipoCartao;

import java.math.BigDecimal;
import java.util.List;

public final class ProdutosDeTeste {

    private ProdutosDeTeste() {
    }

    /** Mesmos valores do enunciado / application.yml. */
    public static CatalogoProdutos catalogoPadrao() {
        return new CatalogoProdutos(List.of(
                new ProdutoCartao(TipoCartao.CARTAO_SEM_ANUIDADE, new BigDecimal("1000"), new BigDecimal("3500"), BigDecimal.ZERO),
                new ProdutoCartao(TipoCartao.CARTAO_DE_PARCEIROS, new BigDecimal("3000"), new BigDecimal("5500"), new BigDecimal("10")),
                new ProdutoCartao(TipoCartao.CARTAO_COM_CASHBACK, new BigDecimal("5000"), new BigDecimal("7500"), new BigDecimal("20"))));
    }
}