package com.desafio.cartoes.domain.regra;

import com.desafio.cartoes.domain.PerfilElegibilidade;
import com.desafio.cartoes.domain.TipoCartao;

import java.util.Optional;
import java.util.Set;

/**
 * Strategy: uma regra de restrição por perfil (idade/UF).
 * Nova regra = nova classe + uma linha em AvaliadorElegibilidade.padrao (Open/Closed).
 */
@FunctionalInterface
public interface RegraDeRestricao {

    /**
     * @return os tipos permitidos se a regra se aplica ao perfil; Optional.empty() se não se aplica.
     */
    Optional<Set<TipoCartao>> tiposPermitidos(PerfilElegibilidade perfil);
}