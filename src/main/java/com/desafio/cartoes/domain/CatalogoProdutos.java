package com.desafio.cartoes.domain;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

/**
 * Conjunto de produtos disponíveis, ordenado pela renda mínima (do mais acessível ao mais exigente).
 */
public class CatalogoProdutos {

    private final List<ProdutoCartao> produtos;

    public CatalogoProdutos(List<ProdutoCartao> produtos) {
        if (produtos == null || produtos.isEmpty()) {
            throw new IllegalArgumentException("O catálogo deve ter ao menos um produto");
        }
        long tiposDistintos = produtos.stream().map(ProdutoCartao::tipo).distinct().count();
        if (tiposDistintos != produtos.size()) {
            throw new IllegalArgumentException("O catálogo não pode ter produtos duplicados");
        }
        this.produtos = produtos.stream()
                .sorted(Comparator.comparing(ProdutoCartao::rendaMinima))
                .toList();
    }

    public List<ProdutoCartao> produtos() {
        return produtos;
    }

    /** Menor renda que dá acesso a algum produto. Abaixo dela, não há o que analisar. */
    public BigDecimal rendaMinimaExigida() {
        return produtos.get(0).rendaMinima();
    }
}