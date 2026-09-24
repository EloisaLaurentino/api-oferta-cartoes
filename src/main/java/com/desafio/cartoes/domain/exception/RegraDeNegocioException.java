package com.desafio.cartoes.domain.exception;

/** Regra de negócio não atendida → HTTP 422. */
public class RegraDeNegocioException extends RuntimeException {

    private final String tipoErro;

    public RegraDeNegocioException(String tipoErro, String mensagem) {
        super(mensagem);
        this.tipoErro = tipoErro;
    }

    public String tipoErro() {
        return tipoErro;
    }
}