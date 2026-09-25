package com.desafio.cartoes.api;

import com.desafio.cartoes.api.ErroResponse.CampoInvalido;
import com.desafio.cartoes.api.ErroResponse.DetalheErro;
import com.desafio.cartoes.domain.exception.IdadeMinimaNaoAtendidaException;
import com.desafio.cartoes.domain.exception.RegraDeNegocioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final String nomeServico;

    public GlobalExceptionHandler(@Value("${spring.application.name}") String nomeServico) {
        this.nomeServico = nomeServico;
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<Object> tratarRegraDeNegocio(RegraDeNegocioException ex) {
        return responder(HttpStatus.UNPROCESSABLE_ENTITY, "Solicitação não pôde ser processada.",
                ex.tipoErro(), ex.getMessage());
    }

    @ExceptionHandler(IdadeMinimaNaoAtendidaException.class)
    public ResponseEntity<Object> tratarIdadeMinima(IdadeMinimaNaoAtendidaException ex) {
        return responder(HttpStatus.BAD_REQUEST, "Requisição inválida.",
                IdadeMinimaNaoAtendidaException.TIPO_ERRO, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> tratarInesperado(Exception ex) {
        log.error("Erro inesperado ao processar requisição", ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Um erro inesperado ocorreu.", "ERRO_INTERNO",
                "Tivemos um problema, mas fique tranquilo que nosso time já foi avisado.");
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        List<CampoInvalido> campos = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new CampoInvalido(
                        paraSnakeCase(e.getField()),
                        Objects.requireNonNullElse(e.getDefaultMessage(), "valor inválido")))
                .sorted(Comparator.comparing(CampoInvalido::campo).thenComparing(CampoInvalido::mensagem))
                .toList();

        return responder(HttpStatus.BAD_REQUEST, "Requisição inválida.", "REQUISICAO_INVALIDA",
                "Um ou mais campos não atendem às regras de validação.", campos, HttpHeaders.EMPTY);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        return responder(HttpStatus.BAD_REQUEST, "Requisição inválida.", "REQUISICAO_INVALIDA",
                "Corpo ausente, JSON malformado ou campo com tipo/formato inválido (ex.: data no padrão yyyy-MM-dd).");
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex,
                                                             Object body,
                                                             HttpHeaders headers,
                                                             HttpStatusCode status,
                                                             WebRequest request) {
        boolean erroDeServidor = status.is5xxServerError();
        if (erroDeServidor) {
            log.error("Erro interno do framework", ex);
        }
        String mensagem = switch (status.value()) {
            case 404 -> "Recurso não encontrado.";
            case 405 -> "Método HTTP não permitido para este recurso.";
            case 406 -> "Formato de resposta não suportado. Use application/json.";
            case 415 -> "Content-Type não suportado. Use application/json.";
            default -> erroDeServidor ? "Um erro inesperado ocorreu." : "Requisição inválida.";
        };
        return responder(status, mensagem, erroDeServidor ? "ERRO_INTERNO" : "REQUISICAO_INVALIDA",
                "A requisição não pôde ser atendida. Verifique método, URL, Content-Type e Accept.",
                null, headers);
    }

    private ResponseEntity<Object> responder(HttpStatusCode status, String mensagem, String tipoErro, String mensagemInterna) {
        return responder(status, mensagem, tipoErro, mensagemInterna, null, HttpHeaders.EMPTY);
    }

    private ResponseEntity<Object> responder(HttpStatusCode status, String mensagem, String tipoErro,
                                             String mensagemInterna, List<CampoInvalido> campos, HttpHeaders headers) {
        var corpo = new ErroResponse(
                String.valueOf(status.value()),
                mensagem,
                new DetalheErro(nomeServico, tipoErro, mensagemInterna, campos));
        return ResponseEntity.status(status)
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .body(corpo);
    }

    static String paraSnakeCase(String campo) {
        return campo.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase();
    }
}