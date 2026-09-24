package com.desafio.cartoes.domain.exception;

/**
 * Cliente abaixo da idade mínima. O enunciado lista essa checagem entre as validações
 * da entrada, então é mapeada para HTTP 400 (e não 422).
 */
public class IdadeMinimaNaoAtendidaException extends RuntimeException {

    public static final String TIPO_ERRO = "IDADE_MINIMA_NAO_ATENDIDA";

    public IdadeMinimaNaoAtendidaException(int idadeMinima) {
        super("O cliente deve ter no mínimo %d anos.".formatted(idadeMinima));
    }
}