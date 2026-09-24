package com.desafio.cartoes.domain.regra;

import com.desafio.cartoes.domain.PerfilElegibilidade;
import com.desafio.cartoes.domain.TipoCartao;

import java.util.Optional;
import java.util.Set;

/** Cliente de 18 a 24 anos ([18, 25)): somente o cartão sem anuidade. */
public class RegraClienteJovem implements RegraDeRestricao {

    @Override
    public Optional<Set<TipoCartao>> tiposPermitidos(PerfilElegibilidade perfil) {
        if (perfil.idadeEntre(18, 25)) {
            return Optional.of(Set.of(TipoCartao.CARTAO_SEM_ANUIDADE));
        }
        return Optional.empty();
    }
}