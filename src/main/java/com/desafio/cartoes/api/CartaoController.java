package com.desafio.cartoes.api;

import com.desafio.cartoes.api.dto.SolicitacaoCartaoRequest;
import com.desafio.cartoes.api.dto.SolicitacaoCartaoResponse;
import com.desafio.cartoes.application.SolicitacaoCartaoService;
import com.desafio.cartoes.domain.Solicitacao;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/cartoes", produces = MediaType.APPLICATION_JSON_VALUE)
public class CartaoController {

    private final SolicitacaoCartaoService service;

    public CartaoController(SolicitacaoCartaoService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SolicitacaoCartaoResponse> solicitar(@Valid @RequestBody SolicitacaoCartaoRequest request) {
        Solicitacao solicitacao = service.solicitar(request.cliente().toDomain());

        if (!solicitacao.possuiCartaoAprovado()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(SolicitacaoCartaoResponse.from(solicitacao));
    }
}