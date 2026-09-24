package com.desafio.cartoes.domain.exception;

public class RendaInsuficienteException extends RegraDeNegocioException {

    public RendaInsuficienteException() {
        super("RENDA_MINIMA_NAO_ATENDIDA",
                "A renda mensal informada não atende aos critérios de análise de crédito.");
    }
}