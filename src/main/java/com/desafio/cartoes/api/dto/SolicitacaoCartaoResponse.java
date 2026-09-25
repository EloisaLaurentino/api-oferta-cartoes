package com.desafio.cartoes.api.dto;

import com.desafio.cartoes.domain.CartaoOfertado;
import com.desafio.cartoes.domain.Cliente;
import com.desafio.cartoes.domain.Solicitacao;
import com.desafio.cartoes.domain.StatusCartao;
import com.desafio.cartoes.domain.TipoCartao;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SolicitacaoCartaoResponse(
        UUID numeroSolicitacao,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS") LocalDateTime dataSolicitacao,
        ClienteResponse cliente,
        List<CartaoOfertadoResponse> cartoesOfertados) {

    public static SolicitacaoCartaoResponse from(Solicitacao solicitacao) {
        return new SolicitacaoCartaoResponse(
                solicitacao.numeroSolicitacao(),
                solicitacao.dataSolicitacao(),
                ClienteResponse.from(solicitacao.cliente()),
                solicitacao.cartoesOfertados().stream().map(CartaoOfertadoResponse::from).toList());
    }

    public record ClienteResponse(
            String nome,
            String cpf,
            int idade,
            LocalDate dataNascimento,
            String uf,
            BigDecimal rendaMensal,
            String email,
            String telefoneWhatsapp) {

        static ClienteResponse from(Cliente c) {
            return new ClienteResponse(c.nome(), c.cpf(), c.idade(), c.dataNascimento(), c.uf(),
                    c.rendaMensal().setScale(2, RoundingMode.HALF_UP),
                    c.email(), c.telefoneWhatsapp());
        }
    }

    public record CartaoOfertadoResponse(
            TipoCartao tipoCartao,
            BigDecimal valorAnuidadeMensal,
            BigDecimal valorLimiteDisponivel,
            StatusCartao status) {

        static CartaoOfertadoResponse from(CartaoOfertado c) {
            return new CartaoOfertadoResponse(c.tipo(), c.valorAnuidadeMensal(), c.valorLimiteDisponivel(), c.status());
        }
    }
}