package br.com.fiap.inovagab.api.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ApiError> naoEncontrado(RecursoNaoEncontradoException ex, HttpServletRequest req) {
        return montar(HttpStatus.NOT_FOUND, ex.getMessage(), req, null);
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<ApiError> regraDeNegocio(RegraDeNegocioException ex, HttpServletRequest req) {
        return montar(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), req, null);
    }

    @ExceptionHandler({AcessoNegadoException.class, AccessDeniedException.class})
    public ResponseEntity<ApiError> acessoNegado(RuntimeException ex, HttpServletRequest req) {
        String msg = ex instanceof AcessoNegadoException
                ? ex.getMessage()
                : "Seu perfil nao tem permissao para esta operacao.";
        return montar(HttpStatus.FORBIDDEN, msg, req, null);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> credenciaisInvalidas(BadCredentialsException ex, HttpServletRequest req) {
        return montar(HttpStatus.UNAUTHORIZED, ex.getMessage(), req, null);
    }

    @ExceptionHandler(IaIndisponivelException.class)
    public ResponseEntity<ApiError> iaIndisponivel(IaIndisponivelException ex, HttpServletRequest req) {
        log.warn("IA indisponivel: {}", ex.getMessage(), ex.getCause());
        return montar(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), req, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validacao(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            campos.put(fe.getField(), fe.getDefaultMessage());
        }
        return montar(HttpStatus.BAD_REQUEST, "Dados invalidos. Verifique os campos.", req, campos);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> corpoInvalido(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return montar(HttpStatus.BAD_REQUEST, "Corpo da requisicao invalido ou mal formatado.", req, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> generico(Exception ex, HttpServletRequest req) {
        log.error("Erro nao tratado em {} {}", req.getMethod(), req.getRequestURI(), ex);
        return montar(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno. Tente novamente.", req, null);
    }

    private ResponseEntity<ApiError> montar(HttpStatus status, String mensagem,
                                            HttpServletRequest req, Map<String, String> campos) {
        ApiError body = new ApiError(Instant.now(), status.value(), status.getReasonPhrase(),
                mensagem, req.getRequestURI(), campos);
        return ResponseEntity.status(status).body(body);
    }
}
