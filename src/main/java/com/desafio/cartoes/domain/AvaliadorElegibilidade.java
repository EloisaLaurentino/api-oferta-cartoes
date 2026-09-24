package com.desafio.cartoes.domain;

import com.desafio.cartoes.domain.regra.RegraClienteJovem;
import com.desafio.cartoes.domain.regra.RegraDeRestricao;
import com.desafio.cartoes.domain.regra.RegraResidenteSp;
import com.desafio.cartoes.domain.regra.RegraResidenteSpEntre25e30Anos;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Decide quais cartões ofertar em duas etapas:
 * 1) RENDA: produto só entra se renda >= renda mínima do produto (dados do catálogo);
 * 2) RESTRIÇÃO DE PERFIL (idade/UF): a PRIMEIRA regra aplicável define os tipos permitidos.
 *    Sem regra aplicável, não há restrição adicional.
 */
public class AvaliadorElegibilidade {

    private final CatalogoProdutos catalogo;
    private final List<RegraDeRestricao> regras;

    public AvaliadorElegibilidade(CatalogoProdutos catalogo, List<RegraDeRestricao> regras) {
        this.catalogo = catalogo;
        this.regras = List.copyOf(regras);
    }

    /**
     * Regras padrão, da mais específica para a mais geral. A ORDEM IMPORTA:
     * SP 25-29 anos (libera tudo) precisa vir antes de "SP" (restringe).
     */
    public static AvaliadorElegibilidade padrao(CatalogoProdutos catalogo) {
        return new AvaliadorElegibilidade(catalogo, List.of(
                new RegraResidenteSpEntre25e30Anos(),
                new RegraClienteJovem(),
                new RegraResidenteSp()));
    }

    public List<CartaoOfertado> avaliar(PerfilElegibilidade perfil) {
        Set<TipoCartao> permitidos = tiposPermitidos(perfil);
        return catalogo.produtos().stream()
                .filter(produto -> produto.rendaSuficiente(perfil.rendaMensal()))
                .filter(produto -> permitidos.contains(produto.tipo()))
                .map(ProdutoCartao::aprovar)
                .toList();
    }

    private Set<TipoCartao> tiposPermitidos(PerfilElegibilidade perfil) {
        return regras.stream()
                .map(regra -> regra.tiposPermitidos(perfil))
                .flatMap(Optional::stream)
                .findFirst()
                .orElseGet(() -> EnumSet.allOf(TipoCartao.class));
    }
}