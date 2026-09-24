package com.desafio.cartoes.domain;

import java.math.BigDecimal;

/**
 * Visão mínima do cliente para decidir elegibilidade: só o que as regras precisam.
 */
public record PerfilElegibilidade(int idade, String uf, BigDecimal rendaMensal) {

    public boolean residenteEm(String ufEsperada) {
        return uf.equalsIgnoreCase(ufEsperada);
    }

    /** Intervalo semiaberto [minInclusivo, maxExclusivo): sem lacunas nem sobreposição entre faixas. */
    public boolean idadeEntre(int minInclusivo, int maxExclusivo) {
        return idade >= minInclusivo && idade < maxExclusivo;
    }
}