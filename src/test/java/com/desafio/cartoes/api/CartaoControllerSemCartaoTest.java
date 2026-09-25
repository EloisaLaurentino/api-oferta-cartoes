package com.desafio.cartoes.api;

import com.desafio.cartoes.application.SolicitacaoCartaoService;
import com.desafio.cartoes.domain.Cliente;
import com.desafio.cartoes.domain.Solicitacao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("POST /cartoes quando nenhum cartão é aprovado")
class CartaoControllerSemCartaoTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SolicitacaoCartaoService service;

    @Test
    @DisplayName("204: sem cartão aprovado responde No Content e sem corpo")
    void deveRetornar204() throws Exception {
        var cliente = new Cliente("Cliente Teste", "123.456.789-10", 26, LocalDate.of(2000, 1, 1), "RJ",
                new BigDecimal("4000"), "cliente@teste.com", "11999992020");
        when(service.solicitar(any())).thenReturn(new Solicitacao(UUID.randomUUID(), LocalDateTime.now(), cliente, List.of()));

        String corpo = """
                {"cliente":{"nome":"Cliente Teste","cpf":"123.456.789-10","idade":26,"data_nascimento":"2000-01-01",
                "uf":"RJ","renda_mensal":4000,"email":"cliente@teste.com","telefone_whatsapp":"11999992020"}}
                """;

        mockMvc.perform(post("/cartoes").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }
}