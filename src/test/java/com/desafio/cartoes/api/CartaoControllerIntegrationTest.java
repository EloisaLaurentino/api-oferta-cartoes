package com.desafio.cartoes.api;

import com.desafio.cartoes.support.RelogioFixoTestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(RelogioFixoTestConfig.class)
@DisplayName("POST /cartoes")
class CartaoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static String payload(String dataNascimento, String uf, String renda) {
        return """
                {
                  "cliente": {
                    "nome": "Cliente Teste",
                    "cpf": "123.456.789-10",
                    "idade": 26,
                    "data_nascimento": "%s",
                    "uf": "%s",
                    "renda_mensal": %s,
                    "email": "cliente@teste.com",
                    "telefone_whatsapp": "11999992020"
                  }
                }
                """.formatted(dataNascimento, uf, renda);
    }

    private ResultActions enviar(String corpo) throws Exception {
        return mockMvc.perform(post("/cartoes").contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    @Test
    @DisplayName("200: renda 4000 fora de SP recebe só o sem anuidade, no formato exato do contrato")
    void deveRetornar200ComCartaoSemAnuidade() throws Exception {
        enviar(payload("2000-01-01", "RJ", "4000"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.numero_solicitacao").isNotEmpty())
                .andExpect(jsonPath("$.data_solicitacao").value("2026-06-15T10:00:00.000"))
                .andExpect(jsonPath("$.cliente.nome").value("Cliente Teste"))
                .andExpect(jsonPath("$.cliente.data_nascimento").value("2000-01-01"))
                .andExpect(jsonPath("$.cliente.telefone_whatsapp").value("11999992020"))
                .andExpect(jsonPath("$.cartoes_ofertados.length()").value(1))
                .andExpect(jsonPath("$.cartoes_ofertados[0].tipo_cartao").value("CARTAO_SEM_ANUIDADE"))
                .andExpect(jsonPath("$.cartoes_ofertados[0].valor_limite_disponivel").value(1000.00))
                .andExpect(jsonPath("$.cartoes_ofertados[0].status").value("APROVADO"))
                .andExpect(content().string(containsString("\"valor_anuidade_mensal\":0.00")))
                .andExpect(content().string(containsString("\"renda_mensal\":4000.00")));
    }

    @Test
    @DisplayName("200: SP com 27 anos e renda alta recebe os 3 cartões (regra SP 25-29 sobrepõe a de SP)")
    void deveOfertarTodosParaSpEntre25e30() throws Exception {
        enviar(payload("1999-01-01", "SP", "10000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartoes_ofertados[*].tipo_cartao").value(contains(
                        "CARTAO_SEM_ANUIDADE", "CARTAO_DE_PARCEIROS", "CARTAO_COM_CASHBACK")));
    }

    @Test
    @DisplayName("200: jovem de 20 anos em SP com renda alta recebe só o sem anuidade")
    void deveOfertarApenasSemAnuidadeParaJovem() throws Exception {
        enviar(payload("2006-03-10", "SP", "10000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartoes_ofertados.length()").value(1))
                .andExpect(jsonPath("$.cartoes_ofertados[0].tipo_cartao").value("CARTAO_SEM_ANUIDADE"));
    }

    @Test
    @DisplayName("422: renda abaixo do mínimo de análise de crédito")
    void deveRetornar422QuandoRendaInsuficiente() throws Exception {
        enviar(payload("2000-01-01", "RJ", "3000"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("422"))
                .andExpect(jsonPath("$.detalhe_erro.app").value("cartoes-api"))
                .andExpect(jsonPath("$.detalhe_erro.tipo_erro").value("RENDA_MINIMA_NAO_ATENDIDA"));
    }

    @Test
    @DisplayName("400: renda mensal negativa")
    void deveRetornar400QuandoRendaNegativa() throws Exception {
        enviar(payload("2000-01-01", "RJ", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("400"))
                .andExpect(jsonPath("$.detalhe_erro.campos_invalidos[?(@.campo == 'cliente.renda_mensal')]").exists());
    }

    @Test
    @DisplayName("400: cliente menor de 18 anos")
    void deveRetornar400QuandoMenorDeIdade() throws Exception {
        enviar(payload("2010-01-01", "RJ", "5000"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhe_erro.tipo_erro").value("IDADE_MINIMA_NAO_ATENDIDA"));
    }

    @Test
    @DisplayName("400: campos obrigatórios ausentes listam cada campo em snake_case")
    void deveRetornar400QuandoCamposObrigatoriosAusentes() throws Exception {
        enviar("{\"cliente\":{}}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhe_erro.campos_invalidos[?(@.campo == 'cliente.nome')]").exists())
                .andExpect(jsonPath("$.detalhe_erro.campos_invalidos[?(@.campo == 'cliente.data_nascimento')]").exists())
                .andExpect(jsonPath("$.detalhe_erro.campos_invalidos[?(@.campo == 'cliente.telefone_whatsapp')]").exists());
    }

    @Test
    @DisplayName("400: objeto cliente ausente")
    void deveRetornar400QuandoClienteAusente() throws Exception {
        enviar("{}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhe_erro.campos_invalidos[?(@.campo == 'cliente')]").exists());
    }

    @Test
    @DisplayName("400: CPF e e-mail em formato inválido")
    void deveRetornar400QuandoFormatoInvalido() throws Exception {
        String corpo = payload("2000-01-01", "RJ", "5000")
                .replace("123.456.789-10", "123")
                .replace("cliente@teste.com", "invalido");

        enviar(corpo)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhe_erro.campos_invalidos[?(@.campo == 'cliente.cpf')]").exists())
                .andExpect(jsonPath("$.detalhe_erro.campos_invalidos[?(@.campo == 'cliente.email')]").exists());
    }

    @Test
    @DisplayName("400: JSON malformado")
    void deveRetornar400QuandoJsonMalformado() throws Exception {
        enviar("{ \"cliente\": ")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhe_erro.tipo_erro").value("REQUISICAO_INVALIDA"));
    }

    @Test
    @DisplayName("400: data de nascimento fora do padrão yyyy-MM-dd")
    void deveRetornar400QuandoDataForaDoPadrao() throws Exception {
        enviar(payload("01/01/2000", "RJ", "5000"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("415: aceita somente JSON, e o erro também sai no formato do contrato")
    void deveRetornar415QuandoContentTypeNaoEhJson() throws Exception {
        mockMvc.perform(post("/cartoes").contentType(MediaType.TEXT_PLAIN).content("x"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.codigo").value("415"));
    }
}