package com.desafio.cartoes.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ProdutoCartao e CatalogoProdutos")
class ProdutoECatalogoTest {

    private static ProdutoCartao produto(TipoCartao tipo, String renda, String anuidade) {
        return new ProdutoCartao(tipo, new BigDecimal("1000"), new BigDecimal(renda), new BigDecimal(anuidade));
    }

    @Test
    @DisplayName("cartão sem anuidade com anuidade diferente de zero é rejeitado")
    void semAnuidadeExigeAnuidadeZero() {
        assertThatThrownBy(() -> produto(TipoCartao.CARTAO_SEM_ANUIDADE, "3500", "5.00"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("valores monetários são normalizados para 2 casas decimais")
    void normalizaCasasDecimais() {
        var p = produto(TipoCartao.CARTAO_SEM_ANUIDADE, "3500", "0");

        assertThat(p.valorAnuidadeMensal().toPlainString()).isEqualTo("0.00");
        assertThat(p.rendaMinima().toPlainString()).isEqualTo("3500.00");
    }

    @Test
    @DisplayName("renda exatamente igual ao mínimo é suficiente; um centavo abaixo não é")
    void limiteDaRenda() {
        var p = produto(TipoCartao.CARTAO_DE_PARCEIROS, "5500", "10");

        assertThat(p.rendaSuficiente(new BigDecimal("5500.00"))).isTrue();
        assertThat(p.rendaSuficiente(new BigDecimal("5499.99"))).isFalse();
    }

    @Test
    @DisplayName("catálogo ordena por renda mínima e expõe a menor renda exigida")
    void catalogoOrdenaPorRenda() {
        var catalogo = new CatalogoProdutos(List.of(
                produto(TipoCartao.CARTAO_COM_CASHBACK, "7500", "20"),
                produto(TipoCartao.CARTAO_SEM_ANUIDADE, "3500", "0"),
                produto(TipoCartao.CARTAO_DE_PARCEIROS, "5500", "10")));

        assertThat(catalogo.produtos()).extracting(ProdutoCartao::tipo).containsExactly(
                TipoCartao.CARTAO_SEM_ANUIDADE, TipoCartao.CARTAO_DE_PARCEIROS, TipoCartao.CARTAO_COM_CASHBACK);
        assertThat(catalogo.rendaMinimaExigida()).isEqualByComparingTo("3500");
    }

    @Test
    @DisplayName("catálogo vazio ou com produtos duplicados impede a subida (fail-fast)")
    void catalogoInvalido() {
        assertThatThrownBy(() -> new CatalogoProdutos(List.of())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CatalogoProdutos(List.of(
                produto(TipoCartao.CARTAO_DE_PARCEIROS, "5500", "10"),
                produto(TipoCartao.CARTAO_DE_PARCEIROS, "6000", "10"))))
                .isInstanceOf(IllegalArgumentException.class);
    }
}