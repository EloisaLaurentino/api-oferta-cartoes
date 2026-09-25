package com.desafio.cartoes.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

public record ErroResponse(String codigo, String mensagem, DetalheErro detalheErro) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record DetalheErro(
            String app,
            String tipoErro,
            String mensagemInterna,
            List<CampoInvalido> camposInvalidos) {
    }

    public record CampoInvalido(String campo, String mensagem) {
    }
}