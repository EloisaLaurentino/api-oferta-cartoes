package com.desafio.cartoes.api;

import com.desafio.cartoes.api.dto.SolicitacaoCartaoRequest;
import com.desafio.cartoes.api.dto.SolicitacaoCartaoResponse;
import com.desafio.cartoes.application.SolicitacaoCartaoService;
import com.desafio.cartoes.domain.Solicitacao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/cartoes", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Cartões", description = "Solicitação e oferta de cartões de crédito")
public class CartaoController {

    private final SolicitacaoCartaoService service;

    public CartaoController(SolicitacaoCartaoService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Solicita a oferta de cartões de crédito para um cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente elegível a pelo menos um cartão"),
            @ApiResponse(responseCode = "204", description = "Cliente sem nenhum cartão aprovado"),
            @ApiResponse(responseCode = "400", description = "Payload inválido"),
            @ApiResponse(responseCode = "422", description = "Regra de negócio não atendida (ex.: renda insuficiente)")
    })
    public ResponseEntity<SolicitacaoCartaoResponse> solicitar(@Valid @RequestBody SolicitacaoCartaoRequest request) {
        Solicitacao solicitacao = service.solicitar(request.cliente().toDomain());

        if (!solicitacao.possuiCartaoAprovado()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(SolicitacaoCartaoResponse.from(solicitacao));
    }
}