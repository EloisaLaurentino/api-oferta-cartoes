package com.desafio.cartoes.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Cliente(
        String nome,
        String cpf,
        int idade,
        LocalDate dataNascimento,
        String uf,
        BigDecimal rendaMensal,
        String email,
        String telefoneWhatsapp) {
}