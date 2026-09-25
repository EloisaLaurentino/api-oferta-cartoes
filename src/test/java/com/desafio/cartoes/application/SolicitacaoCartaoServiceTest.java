package com.desafio.cartoes.application;

import com.desafio.cartoes.config.CartoesProperties;
import com.desafio.cartoes.domain.AvaliadorElegibilidade;
import com.desafio.cartoes.domain.CatalogoProdutos;
import com.desafio.cartoes.domain.Cliente;
import com.desafio.cartoes.domain.Solicitacao;
import com.desafio.cartoes.domain.TipoCartao;
import com.desafio.cartoes.domain.exception.IdadeMinimaNaoAtendidaException;
import com.desafio.cartoes.domain.exception.RendaInsuficienteException;
import com.desafio.cartoes.support.ProdutosDeTeste;
import com.desafio.cartoes.support.RelogioFixoTestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** "Hoje" nos testes = 15/06/2026 (ver RelogioFixoTestConfig). */
@DisplayName("SolicitacaoCartaoService")
class SolicitacaoCartaoServiceTest {

    private final CatalogoProdutos catalogo = ProdutosDeTeste.catalogoPadrao();
    private final CartoesProperties propriedades = new CartoesProperties(18, List.of());
    private final SolicitacaoCartaoService service = new SolicitacaoCartaoService(
            AvaliadorElegibilidade.padrao(catalogo), catalogo, propriedades, RelogioFixoTestConfig.RELOGIO);

    private static Cliente cliente(LocalDate nascimento, String uf, String renda) {
        return new Cliente("Cliente Teste", "123.456.789-10", 0, nascimento, uf,
                new BigDecimal(renda), "cliente@teste.com", "11999992020");
    }

    @Test
    @DisplayName("gera número (UUID) e data da solicitação a partir do relógio injetado")
    void geraNumeroEData() {
        Solicitacao s = service.solicitar(cliente(LocalDate.of(2000, 1, 1), "RJ", "4000"));

        assertThat(s.numeroSolicitacao()).isNotNull();
        assertThat(s.dataSolicitacao()).isEqualTo(LocalDateTime.of(2026, 6, 15, 10, 0, 0));
        assertThat(s.possuiCartaoAprovado()).isTrue();
        assertThat(s.cartoesOfertados()).extracting(c -> c.tipo()).containsExactly(TipoCartao.CARTAO_SEM_ANUIDADE);
    }

    @Test
    @DisplayName("aceita quem completa 18 anos exatamente hoje")
    void aceitaQuemFazAniversarioHoje() {
        Solicitacao s = service.solicitar(cliente(LocalDate.of(2008, 6, 15), "RJ", "4000"));

        assertThat(s.possuiCartaoAprovado()).isTrue();
    }

    @Test
    @DisplayName("rejeita quem completa 18 anos amanhã (falta 1 dia)")
    void rejeitaFaltandoUmDia() {
        assertThatThrownBy(() -> service.solicitar(cliente(LocalDate.of(2008, 6, 16), "RJ", "4000")))
                .isInstanceOf(IdadeMinimaNaoAtendidaException.class);
    }

    @Test
    @DisplayName("a idade mínima vem da configuração (21 anos: quem tem 20 é rejeitado)")
    void idadeMinimaConfiguravel() {
        var exigente = new SolicitacaoCartaoService(AvaliadorElegibilidade.padrao(catalogo), catalogo,
                new CartoesProperties(21, List.of()), RelogioFixoTestConfig.RELOGIO);

        assertThatThrownBy(() -> exigente.solicitar(cliente(LocalDate.of(2006, 1, 1), "RJ", "4000")))
                .isInstanceOf(IdadeMinimaNaoAtendidaException.class);
    }

    @Test
    @DisplayName("renda abaixo da menor renda do catálogo gera RendaInsuficienteException (422)")
    void rendaInsuficiente() {
        assertThatThrownBy(() -> service.solicitar(cliente(LocalDate.of(2000, 1, 1), "RJ", "3499.99")))
                .isInstanceOf(RendaInsuficienteException.class);
    }

    @Test
    @DisplayName("renda exatamente no mínimo é aceita")
    void rendaNoLimite() {
        Solicitacao s = service.solicitar(cliente(LocalDate.of(2000, 1, 1), "RJ", "3500.00"));

        assertThat(s.possuiCartaoAprovado()).isTrue();
    }

    @Test
    @DisplayName("quando as regras eliminam todos os cartões, não há cartão aprovado (base do HTTP 204)")
    void semCartaoAprovado() {
        var bloqueiaTudo = new AvaliadorElegibilidade(catalogo, List.of(perfil -> Optional.of(Set.of())));
        var svc = new SolicitacaoCartaoService(bloqueiaTudo, catalogo, propriedades, RelogioFixoTestConfig.RELOGIO);

        Solicitacao s = svc.solicitar(cliente(LocalDate.of(2000, 1, 1), "RJ", "10000"));

        assertThat(s.cartoesOfertados()).isEmpty();
        assertThat(s.possuiCartaoAprovado()).isFalse();
    }
}