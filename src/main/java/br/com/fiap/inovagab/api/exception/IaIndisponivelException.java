package br.com.fiap.inovagab.api.exception;

public class IaIndisponivelException extends RuntimeException {
    public IaIndisponivelException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
