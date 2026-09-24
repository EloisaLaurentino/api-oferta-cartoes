package com.desafio.cartoes.domain.regra;

import com.desafio.cartoes.domain.PerfilElegibilidade;
import com.desafio.cartoes.domain.TipoCartao;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

/** Reside em SP e tem de 25 a 29 anos ([25, 30)): todos os cartões podem ser ofertados. */
public class RegraResidenteSpEntre25e30Anos implements RegraDeRestricao {

    @Override
    public Optional<Set<TipoCartao>> tiposPermitidos(PerfilElegibilidade perfil) {
        if (perfil.residenteEm("SP") && perfil.idadeEntre(25, 30)) {
            return Optional.of(EnumSet.allOf(TipoCartao.class));
        }
        return Optional.empty();
    }
}