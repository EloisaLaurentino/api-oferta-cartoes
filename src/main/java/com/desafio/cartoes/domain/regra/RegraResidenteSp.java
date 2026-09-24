package com.desafio.cartoes.domain.regra;

import com.desafio.cartoes.domain.PerfilElegibilidade;
import com.desafio.cartoes.domain.TipoCartao;

import java.util.Optional;
import java.util.Set;

/** Reside em SP: somente cashback e sem anuidade (a exceção 25-29 anos é tratada por regra anterior). */
public class RegraResidenteSp implements RegraDeRestricao {

    @Override
    public Optional<Set<TipoCartao>> tiposPermitidos(PerfilElegibilidade perfil) {
        if (perfil.residenteEm("SP")) {
            return Optional.of(Set.of(TipoCartao.CARTAO_COM_CASHBACK, TipoCartao.CARTAO_SEM_ANUIDADE));
        }
        return Optional.empty();
    }
}