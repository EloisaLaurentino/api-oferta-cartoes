package com.desafio.cartoes.api;

import com.desafio.cartoes.application.SolicitacaoCartaoService;
import com.desafio.cartoes.domain.CartaoOfertado;
import com.desafio.cartoes.domain.Cliente;
import com.desafio.cartoes.domain.Solicitacao;
import com.desafio.cartoes.domain.StatusCartao;
import com.desafio.cartoes.domain.TipoCartao;
import com.desafio.cartoes.domain.exception.IdadeMinimaNaoAtendidaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartaoController.class)
class CartaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SolicitacaoCartaoService service;

    @Test
    @DisplayName("Deve retornar 200 OK quando a solicitação tiver cartão aprovado")
    void deveRetornar200QuandoCartaoAprovado() throws Exception {
        Cliente cliente = new Cliente("Teste Silva", "05437891082", 25, LocalDate.of(1999, 1, 1), "SP", new BigDecimal("3000.00"), "teste@email.com", "11999998888");
        CartaoOfertado cartao = new CartaoOfertado(TipoCartao.CARTAO_SEM_ANUIDADE, new BigDecimal("10.00"), new BigDecimal("1000.00"), StatusCartao.APROVADO);
        Solicitacao solicitacao = new Solicitacao(UUID.randomUUID(), LocalDateTime.now(), cliente, List.of(cartao));

        when(service.solicitar(any())).thenReturn(solicitacao);

        String jsonPayload = """
                {
                  "cliente": {
                    "nome": "Teste Silva",
                    "cpf": "05437891082",
                    "idade": 25,
                    "data_nascimento": "1999-01-01",
                    "uf": "SP",
                    "renda_mensal": 3000.00,
                    "email": "teste@email.com",
                    "telefone_whatsapp": "11999998888"
                  }
                }
                """;

        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve retornar 204 No Content quando não houver cartões aprovados")
    void deveRetornar204QuandoSemCartoesAprovados() throws Exception {
        Cliente cliente = new Cliente("Teste Silva", "05437891082", 25, LocalDate.of(1999, 1, 1), "SP", new BigDecimal("1000.00"), "teste@email.com", "11999998888");
        Solicitacao solicitacao = new Solicitacao(UUID.randomUUID(), LocalDateTime.now(), cliente, List.of());

        when(service.solicitar(any())).thenReturn(solicitacao);

        String jsonPayload = """
                {
                  "cliente": {
                    "nome": "Teste Silva",
                    "cpf": "05437891082",
                    "idade": 25,
                    "data_nascimento": "1999-01-01",
                    "uf": "SP",
                    "renda_mensal": 1000.00,
                    "email": "teste@email.com",
                    "telefone_whatsapp": "11999998888"
                  }
                }
                """;

        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request com campos inválidos em formato snake_case")
    void deveRetornar400ComCamposInvalidos() throws Exception {
        String jsonPayload = """
                {
                  "cliente": {
                    "nome": "",
                    "cpf": "cpf-invalido",
                    "idade": -5,
                    "data_nascimento": "2030-01-01",
                    "uf": "SAO_PAULO",
                    "renda_mensal": -100,
                    "email": "email-invalido",
                    "telefone_whatsapp": "123"
                  }
                }
                """;

        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("400"));
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request quando lançar IdadeMinimaNaoAtendidaException")
    void deveTratarIdadeMinimaException() throws Exception {
        when(service.solicitar(any())).thenThrow(new IdadeMinimaNaoAtendidaException(18));

        String jsonPayload = """
                {
                  "cliente": {
                    "nome": "Menor de Idade",
                    "cpf": "05437891082",
                    "idade": 16,
                    "data_nascimento": "2008-01-01",
                    "uf": "SP",
                    "renda_mensal": 2000.00,
                    "email": "menor@email.com",
                    "telefone_whatsapp": "11999998888"
                  }
                }
                """;

        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest());
    }
}