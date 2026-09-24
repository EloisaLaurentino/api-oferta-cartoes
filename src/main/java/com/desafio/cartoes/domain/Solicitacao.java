package com.desafio.cartoes.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record Solicitacao(
        UUID numeroSolicitacao,
        LocalDateTime dataSolicitacao,
        Cliente cliente,
        List<CartaoOfertado> cartoesOfertados) {

    public boolean possuiCartaoAprovado() {
        return cartoesOfertados.stream().anyMatch(c -> c.status() == StatusCartao.APROVADO);
    }
}