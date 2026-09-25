package com.desafio.cartoes.application;

import com.desafio.cartoes.config.CartoesProperties;
import com.desafio.cartoes.domain.AvaliadorElegibilidade;
import com.desafio.cartoes.domain.CartaoOfertado;
import com.desafio.cartoes.domain.CatalogoProdutos;
import com.desafio.cartoes.domain.Cliente;
import com.desafio.cartoes.domain.PerfilElegibilidade;
import com.desafio.cartoes.domain.Solicitacao;
import com.desafio.cartoes.domain.exception.IdadeMinimaNaoAtendidaException;
import com.desafio.cartoes.domain.exception.RendaInsuficienteException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * Caso de uso: processar uma solicitação de cartão.
 * Orquestra (valida pré-condições, monta o perfil, chama o domínio) e não conhece HTTP.
 */
@Service
public class SolicitacaoCartaoService {

    private final AvaliadorElegibilidade avaliador;
    private final CatalogoProdutos catalogo;
    private final CartoesProperties propriedades;
    private final Clock clock;

    public SolicitacaoCartaoService(AvaliadorElegibilidade avaliador,
                                    CatalogoProdutos catalogo,
                                    CartoesProperties propriedades,
                                    Clock clock) {
        this.avaliador = avaliador;
        this.catalogo = catalogo;
        this.propriedades = propriedades;
        this.clock = clock;
    }

    public Solicitacao solicitar(Cliente cliente) {
        int idade = calcularIdade(cliente.dataNascimento());

        if (idade < propriedades.idadeMinima()) {
            throw new IdadeMinimaNaoAtendidaException(propriedades.idadeMinima());
        }
        if (cliente.rendaMensal().compareTo(catalogo.rendaMinimaExigida()) < 0) {
            throw new RendaInsuficienteException();
        }

        var perfil = new PerfilElegibilidade(idade, cliente.uf(), cliente.rendaMensal());
        List<CartaoOfertado> cartoes = avaliador.avaliar(perfil);

        return new Solicitacao(
                UUID.randomUUID(),
                LocalDateTime.now(clock).truncatedTo(ChronoUnit.MILLIS),
                cliente,
                cartoes);
    }

    private int calcularIdade(LocalDate dataNascimento) {
        return Period.between(dataNascimento, LocalDate.now(clock)).getYears();
    }
}