package br.com.fiap.inovagab.api.service;

import br.com.fiap.inovagab.api.domain.Recompensa;
import br.com.fiap.inovagab.api.domain.Resgate;
import br.com.fiap.inovagab.api.domain.Usuario;
import br.com.fiap.inovagab.api.dto.ResgateResponse;
import br.com.fiap.inovagab.api.exception.RecursoNaoEncontradoException;
import br.com.fiap.inovagab.api.exception.RegraDeNegocioException;
import br.com.fiap.inovagab.api.repository.RecompensaRepository;
import br.com.fiap.inovagab.api.repository.ResgateRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class RecompensaService {

    private final RecompensaRepository recompensaRepository;
    private final ResgateRepository resgateRepository;
    private final PontosService pontosService;

    public RecompensaService(RecompensaRepository recompensaRepository, ResgateRepository resgateRepository,
                             PontosService pontosService) {
        this.recompensaRepository = recompensaRepository;
        this.resgateRepository = resgateRepository;
        this.pontosService = pontosService;
    }

    public List<Recompensa> listar() {
        return recompensaRepository.findByDisponivelTrueOrderByCustoAsc();
    }

    public List<Resgate> meusResgates(Usuario operador) {
        return resgateRepository.findByOperadorIdOrderByResgatadoEmDesc(operador.getId());
    }

    public ResgateResponse resgatar(String recompensaId, Usuario operador) {
        Recompensa recompensa = recompensaRepository.findById(recompensaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Recompensa nao encontrada: " + recompensaId));
        if (!recompensa.isDisponivel()) {
            throw new RegraDeNegocioException("Recompensa indisponivel no momento.");
        }

        Usuario atualizado = pontosService.debitarSeHouverSaldo(operador.getId(), recompensa.getCusto());
        if (atualizado == null) {
            throw new RegraDeNegocioException("Saldo insuficiente. Voce tem " + operador.getPontos()
                    + " ponto(s) e precisa de " + recompensa.getCusto() + ".");
        }

        Resgate resgate = resgateRepository.save(Resgate.builder()
                .recompensaId(recompensa.getId())
                .recompensaTitulo(recompensa.getTitulo())
                .recompensaEmoji(recompensa.getEmoji())
                .custoPago(recompensa.getCusto())
                .operadorId(operador.getId())
                .operadorNome(operador.getNome())
                .resgatadoEm(Instant.now())
                .status("confirmado")
                .build());

        return new ResgateResponse(resgate, atualizado.getPontos());
    }
}
