package com.desafio.cartoes.domain;

import com.desafio.cartoes.support.ProdutosDeTeste;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AvaliadorElegibilidade")
class AvaliadorElegibilidadeTest {

    // S = sem anuidade | P = parceiros | C = cashback
    private static final Map<String, TipoCartao> SIGLAS = Map.of(
            "S", TipoCartao.CARTAO_SEM_ANUIDADE,
            "P", TipoCartao.CARTAO_DE_PARCEIROS,
            "C", TipoCartao.CARTAO_COM_CASHBACK);

    private final AvaliadorElegibilidade avaliador = AvaliadorElegibilidade.padrao(ProdutosDeTeste.catalogoPadrao());

    @ParameterizedTest(name = "idade={0}, uf={1}, renda={2} => {3}")
    @DisplayName("oferta somente os cartões permitidos pela combinação de renda, idade e UF")
    @CsvSource(delimiter = '|', value = {
            // --- só renda (fora de SP, 30+ anos: nenhuma restrição de perfil) ---
            "30|RJ|3500.00|S",
            "30|RJ|5499.99|S",
            "30|RJ|5500.00|S,P",
            "30|RJ|7499.99|S,P",
            "30|RJ|7500.00|S,P,C",
            // --- 18 a 24 anos: somente sem anuidade, mesmo com renda alta ---
            "18|MG|10000.00|S",
            "24|MG|10000.00|S",
            // --- 25 fora de SP: faixa jovem acabou, sem restrição ---
            "25|MG|10000.00|S,P,C",
            // --- SP genérico: só cashback e sem anuidade (parceiros bloqueado) ---
            "30|SP|10000.00|S,C",
            "40|SP|7500.00|S,C",
            "40|SP|6000.00|S",
            // --- SP entre 25 e 29 anos: libera tudo (limites da faixa) ---
            "25|SP|10000.00|S,P,C",
            "27|SP|10000.00|S,P,C",
            "29|SP|5500.00|S,P",
            // --- SP + jovem: a regra de idade (mais restritiva) vence ---
            "22|SP|10000.00|S",
            // --- UF em minúsculo é tratada como SP ---
            "45|sp|10000.00|S,C"
    })
    void ofertaConformeRegras(int idade, String uf, BigDecimal renda, String esperado) {
        var perfil = new PerfilElegibilidade(idade, uf, renda);

        List<TipoCartao> ofertados = avaliador.avaliar(perfil).stream().map(CartaoOfertado::tipo).toList();

        List<TipoCartao> esperados = Arrays.stream(esperado.split(",")).map(SIGLAS::get).toList();
        assertThat(ofertados).containsExactlyElementsOf(esperados);
    }

    @Test
    @DisplayName("não oferta nada quando a renda está abaixo do menor produto")
    void semCartaoQuandoRendaAbaixoDoMinimo() {
        var perfil = new PerfilElegibilidade(30, "RJ", new BigDecimal("3499.99"));

        assertThat(avaliador.avaliar(perfil)).isEmpty();
    }

    @Test
    @DisplayName("todo cartão ofertado sai APROVADO com limite e anuidade do catálogo")
    void cartaoOfertadoTrazDadosDoCatalogo() {
        var perfil = new PerfilElegibilidade(30, "RJ", new BigDecimal("3500"));

        CartaoOfertado cartao = avaliador.avaliar(perfil).get(0);

        assertThat(cartao.status()).isEqualTo(StatusCartao.APROVADO);
        assertThat(cartao.valorLimiteDisponivel()).isEqualByComparingTo("1000.00");
        assertThat(cartao.valorAnuidadeMensal().toPlainString()).isEqualTo("0.00");
    }
}