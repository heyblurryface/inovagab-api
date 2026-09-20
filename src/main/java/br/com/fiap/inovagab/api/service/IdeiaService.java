package br.com.fiap.inovagab.api.service;

import br.com.fiap.inovagab.api.domain.AvaliacaoIa;
import br.com.fiap.inovagab.api.domain.Ideia;
import br.com.fiap.inovagab.api.domain.Orientacao;
import br.com.fiap.inovagab.api.domain.Role;
import br.com.fiap.inovagab.api.domain.StatusIdeia;
import br.com.fiap.inovagab.api.domain.Usuario;
import br.com.fiap.inovagab.api.dto.IdeiaRequest;
import br.com.fiap.inovagab.api.exception.AcessoNegadoException;
import br.com.fiap.inovagab.api.exception.RecursoNaoEncontradoException;
import br.com.fiap.inovagab.api.exception.RegraDeNegocioException;
import br.com.fiap.inovagab.api.ia.AvaliadorHeuristico;
import br.com.fiap.inovagab.api.ia.IaService;
import br.com.fiap.inovagab.api.repository.IdeiaRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class IdeiaService {

    private final IdeiaRepository repository;
    private final OrientacaoService orientacaoService;
    private final PontosService pontosService;
    private final IaService iaService;

    public IdeiaService(IdeiaRepository repository, OrientacaoService orientacaoService,
                        PontosService pontosService, IaService iaService) {
        this.repository = repository;
        this.orientacaoService = orientacaoService;
        this.pontosService = pontosService;
        this.iaService = iaService;
    }

    // ---------- consultas ----------

    public List<Ideia> listarMinhas(Usuario autor) {
        return repository.findByAutorIdOrderByCriadoEmDesc(autor.getId());
    }

    public List<Ideia> listar(StatusIdeia status, String orientacaoId) {
        List<Ideia> base = status != null
                ? repository.findByStatusOrderByCriadoEmDesc(status)
                : repository.findAllByOrderByCriadoEmDesc();
        if (orientacaoId != null && !orientacaoId.isBlank()) {
            return base.stream().filter(i -> orientacaoId.equals(i.getOrientacaoId())).toList();
        }
        return base;
    }

    /** Ideias aprovadas ordenadas por prioridade (usadas pelo gestor ao cadastrar projetos). */
    public List<Ideia> listarAprovadasPorPrioridade() {
        return repository.findByStatusOrderByCriadoEmDesc(StatusIdeia.APROVADA).stream()
                .sorted(Comparator.comparingInt(Ideia::getPrioridade).reversed())
                .toList();
    }

    /** Ranking das ideias avaliadas por IA (maior pontuacao primeiro). */
    public List<Ideia> ranking() {
        return repository.findByAvaliacaoIaIsNotNull().stream()
                .sorted(Comparator.comparingInt((Ideia i) -> i.getAvaliacaoIa().getPontuacao()).reversed())
                .toList();
    }

    public Ideia buscar(String id, Usuario solicitante) {
        Ideia ideia = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ideia nao encontrada: " + id));
        if (solicitante.getRole() == Role.OPERADOR && !ideia.getAutorId().equals(solicitante.getId())) {
            throw new AcessoNegadoException("Voce so pode consultar suas proprias ideias.");
        }
        return ideia;
    }

    // ---------- operador ----------

    public Ideia criar(IdeiaRequest req, Usuario autor) {
        Orientacao orientacao = resolverOrientacao(req.orientacaoId());
        Instant agora = Instant.now();
        Ideia ideia = Ideia.builder()
                .titulo(req.titulo().trim())
                .descricao(req.descricao().trim())
                .pilar(req.pilar())
                .orientacaoId(orientacao != null ? orientacao.getId() : null)
                .orientacaoTitulo(orientacao != null ? orientacao.getTitulo() : null)
                .autorId(autor.getId())
                .autorNome(autor.getNome())
                .status(StatusIdeia.PENDENTE)
                .prioridade(0)
                .criadoEm(agora)
                .atualizadoEm(agora)
                .build();
        Ideia salva = repository.save(ideia);
        pontosService.creditar(autor.getId(), PontosService.PONTOS_IDEIA_CADASTRADA);
        return salva;
    }

    public Ideia atualizar(String id, IdeiaRequest req, Usuario autor) {
        Ideia ideia = buscarDoAutorPendente(id, autor);
        Orientacao orientacao = resolverOrientacao(req.orientacaoId());
        ideia.setTitulo(req.titulo().trim());
        ideia.setDescricao(req.descricao().trim());
        ideia.setPilar(req.pilar());
        ideia.setOrientacaoId(orientacao != null ? orientacao.getId() : null);
        ideia.setOrientacaoTitulo(orientacao != null ? orientacao.getTitulo() : null);
        ideia.setAvaliacaoIa(null); // conteudo mudou: avaliacao anterior deixa de valer
        ideia.setAtualizadoEm(Instant.now());
        return repository.save(ideia);
    }

    public void excluir(String id, Usuario autor) {
        Ideia ideia = buscarDoAutorPendente(id, autor);
        repository.delete(ideia);
    }

    // ---------- gestor ----------

    public Ideia definirPrioridade(String id, int prioridade) {
        Ideia ideia = buscarOuFalhar(id);
        ideia.setPrioridade(prioridade);
        ideia.setAtualizadoEm(Instant.now());
        return repository.save(ideia);
    }

    /** Muda o status; credita +50 pontos ao autor apenas na primeira aprovacao (idempotente). */
    public Ideia definirStatus(String id, StatusIdeia novoStatus) {
        Ideia ideia = buscarOuFalhar(id);
        boolean primeiraAprovacao = novoStatus == StatusIdeia.APROVADA && ideia.getStatus() != StatusIdeia.APROVADA;

        ideia.setStatus(novoStatus);
        ideia.setAtualizadoEm(Instant.now());
        Ideia salva = repository.save(ideia);

        if (primeiraAprovacao) {
            pontosService.creditar(ideia.getAutorId(), PontosService.PONTOS_IDEIA_APROVADA);
        }
        return salva;
    }

    /** IA: pontua a ideia e, se aplicarPrioridade, grava a prioridade sugerida. */
    public Ideia avaliarComIa(String id, boolean aplicarPrioridade) {
        Ideia ideia = buscarOuFalhar(id);
        List<Orientacao> vigentes = orientacaoService.listarVigentes();
        AvaliacaoIa avaliacao = iaService.avaliar(ideia, vigentes);
        ideia.setAvaliacaoIa(avaliacao);
        if (aplicarPrioridade) {
            ideia.setPrioridade(avaliacao.getPrioridadeSugerida());
        }
        ideia.setAtualizadoEm(Instant.now());
        return repository.save(ideia);
    }

    /**
     * IA em lote: avalia as ideias pendentes que ainda nao tem avaliacao e tambem as que
     * so tem o resultado do avaliador heuristico local, para que uma indisponibilidade
     * temporaria da Gemini possa ser reprocessada depois.
     */
    public List<Ideia> avaliarPendentesComIa(boolean aplicarPrioridade) {
        List<Ideia> pendentes = repository.findByStatusOrderByCriadoEmDesc(StatusIdeia.PENDENTE)
                .stream()
                .filter(IdeiaService::precisaAvaliacaoGenerativa)
                .toList();
        List<Ideia> avaliadas = new ArrayList<>();
        for (int idx = 0; idx < pendentes.size(); idx++) {
            if (idx > 0) {
                // Espaca as chamadas: o plano gratuito da Gemini limita requisicoes por minuto
                // e um lote disparado em rajada faz as seguintes caírem no fallback.
                dormir(PAUSA_ENTRE_AVALIACOES_MS);
            }
            avaliadas.add(avaliarComIa(pendentes.get(idx).getId(), aplicarPrioridade));
        }
        avaliadas.sort(Comparator.comparingInt((Ideia i) -> i.getAvaliacaoIa().getPontuacao()).reversed());
        return avaliadas;
    }

    // ---------- helpers ----------

    /** Intervalo entre as chamadas de IA dentro de um lote. */
    private static final long PAUSA_ENTRE_AVALIACOES_MS = 4000L;

    private static void dormir(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** Sem avaliacao, ou avaliada apenas pela heuristica local (sem IA generativa). */
    private static boolean precisaAvaliacaoGenerativa(Ideia ideia) {
        AvaliacaoIa avaliacao = ideia.getAvaliacaoIa();
        return avaliacao == null
                || avaliacao.getModelo() == null
                || avaliacao.getModelo().isBlank()
                || AvaliadorHeuristico.MODELO.equals(avaliacao.getModelo());
    }

    private Ideia buscarOuFalhar(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ideia nao encontrada: " + id));
    }

    private Ideia buscarDoAutorPendente(String id, Usuario autor) {
        Ideia ideia = buscarOuFalhar(id);
        if (!ideia.getAutorId().equals(autor.getId())) {
            throw new AcessoNegadoException("Voce so pode alterar suas proprias ideias.");
        }
        if (ideia.getStatus() != StatusIdeia.PENDENTE) {
            throw new RegraDeNegocioException("Ideias ja avaliadas pelo gestor nao podem ser alteradas ou excluidas.");
        }
        return ideia;
    }

    private Orientacao resolverOrientacao(String orientacaoId) {
        if (orientacaoId == null || orientacaoId.isBlank()) return null;
        Orientacao o = orientacaoService.buscar(orientacaoId);
        if (!o.isAtiva()) {
            throw new RegraDeNegocioException("A orientacao informada nao esta mais vigente.");
        }
        return o;
    }
}
