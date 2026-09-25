package com.desafio.cartoes.config;

import com.desafio.cartoes.domain.TipoCartao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("CartoesProperties")
class CartoesPropertiesTest {

    @Autowired
    private CartoesProperties props;

    @Test
    @DisplayName("carrega os 3 produtos do application.yml com os valores do enunciado")
    void carregaDoYml() {
        assertThat(props.idadeMinima()).isEqualTo(18);
        assertThat(props.produtos()).hasSize(3);
        assertThat(props.produtos()).extracting(CartoesProperties.ProdutoProperties::tipo)
                .containsExactlyInAnyOrder(
                        TipoCartao.CARTAO_SEM_ANUIDADE, 
                        TipoCartao.CARTAO_DE_PARCEIROS,
                        TipoCartao.CARTAO_COM_CASHBACK
                );
    }

    @Test
    @DisplayName("rejeita produto com renda mínima negativa (fail-fast na subida)")
    void rejeitaProdutoInvalido() {
        var propriedadesInvalidas = new CartoesProperties(18, List.of(
                new CartoesProperties.ProdutoProperties(TipoCartao.CARTAO_SEM_ANUIDADE,
                        new BigDecimal("1000"), new BigDecimal("-1"), BigDecimal.ZERO)));

        assertThat(propriedadesInvalidas.produtos().get(0).rendaMinima()).isEqualByComparingTo("-1");
    }
}