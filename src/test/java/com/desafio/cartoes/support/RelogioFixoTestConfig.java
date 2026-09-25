package com.desafio.cartoes.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

/**
 * "Hoje" congelado em 15/06/2026 (America/Sao_Paulo) para os testes de idade serem determinísticos.
 */
@TestConfiguration(proxyBeanMethods = false)
public class RelogioFixoTestConfig {

    public static final Clock RELOGIO =
            Clock.fixed(Instant.parse("2026-06-15T13:00:00Z"), ZoneId.of("America/Sao_Paulo"));

    @Bean
    @Primary
    public Clock relogioFixo() {
        return RELOGIO;
    }
}