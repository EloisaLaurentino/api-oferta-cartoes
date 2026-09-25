package com.desafio.cartoes.api.dto;

import com.desafio.cartoes.domain.Cliente;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SolicitacaoCartaoRequest(
        @NotNull(message = SolicitacaoCartaoRequest.OBRIGATORIO) @Valid ClienteRequest cliente) {

    static final String OBRIGATORIO = "campo obrigatório";
    static final String FORMATO_INVALIDO = "formato inválido";

    public record ClienteRequest(
            @NotBlank(message = OBRIGATORIO)
            String nome,

            @NotBlank(message = OBRIGATORIO)
            @Pattern(regexp = "^(\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}|\\d{11})$", message = FORMATO_INVALIDO)
            String cpf,

            @NotNull(message = OBRIGATORIO)
            @PositiveOrZero(message = "não pode ser negativo")
            Integer idade,

            @NotNull(message = OBRIGATORIO)
            @Past(message = "deve ser uma data no passado")
            LocalDate dataNascimento,

            @NotBlank(message = OBRIGATORIO)
            @Pattern(regexp = "^[A-Za-z]{2}$", message = "deve ter 2 letras (ex.: SP)")
            String uf,

            @NotNull(message = OBRIGATORIO)
            @PositiveOrZero(message = "não pode ser negativo")
            BigDecimal rendaMensal,

            @NotBlank(message = OBRIGATORIO)
            @Email(message = "e-mail inválido")
            String email,

            @NotBlank(message = OBRIGATORIO)
            @Pattern(regexp = "^\\d{10,11}$", message = "deve conter 10 ou 11 dígitos, somente números")
            String telefoneWhatsapp) {

        public Cliente toDomain() {
            return new Cliente(nome, cpf, idade, dataNascimento, uf, rendaMensal, email, telefoneWhatsapp);
        }
    }
}