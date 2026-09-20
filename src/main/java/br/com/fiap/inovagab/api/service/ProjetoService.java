package br.com.fiap.inovagab.api.service;

import br.com.fiap.inovagab.api.domain.EtapaProjeto;
import br.com.fiap.inovagab.api.domain.Ideia;
import br.com.fiap.inovagab.api.domain.Orientacao;
import br.com.fiap.inovagab.api.domain.Projeto;
import br.com.fiap.inovagab.api.domain.StatusProjeto;
import br.com.fiap.inovagab.api.domain.Usuario;
import br.com.fiap.inovagab.api.dto.ProjetoRequest;
import br.com.fiap.inovagab.api.exception.RecursoNaoEncontradoException;
import br.com.fiap.inovagab.api.exception.RegraDeNegocioException;
import br.com.fiap.inovagab.api.repository.IdeiaRepository;
import br.com.fiap.inovagab.api.repository.ProjetoRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class ProjetoService {

    private final ProjetoRepository repository;
    private final IdeiaRepository ideiaRepository;
    private final OrientacaoService orientacaoService;

    public ProjetoService(ProjetoRepository repository, IdeiaRepository ideiaRepository,
                          OrientacaoService orientacaoService) {
        this.repository = repository;
        this.ideiaRepository = ideiaRepository;
        this.orientacaoService = orientacaoService;
    }

    public List<Projeto> listar(StatusProjeto status, String orientacaoId) {
        return repository.findAllByOrderByCriadoEmDesc().stream()
                .filter(p -> status == null || p.getStatus() == status)
                .filter(p -> orientacaoId == null || orientacaoId.isBlank() || orientacaoId.equals(p.getOrientacaoId()))
                .toList();
    }

    public Projeto buscar(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Projeto nao encontrado: " + id));
    }

    public Projeto criar(ProjetoRequest req, Usuario gestor) {
        Instant agora = Instant.now();
        Projeto p = Projeto.builder()
                .gestorId(gestor.getId())
                .gestorNome(gestor.getNome())
                .criadoEm(agora)
                .build();
        aplicar(p, req);
        p.setAtualizadoEm(agora);
        return repository.save(p);
    }

    public Projeto atualizar(String id, ProjetoRequest req) {
        Projeto p = buscar(id);
        aplicar(p, req);
        p.setAtualizadoEm(Instant.now());
        return repository.save(p);
    }

    public void excluir(String id) {
        Projeto p = buscar(id);
        if (p.getStatus() == StatusProjeto.CONCLUIDO) {
            throw new RegraDeNegocioException("Projetos concluidos nao podem ser excluidos (mantidos para o historico de resultados).");
        }
        repository.delete(p);
    }

    private void aplicar(Projeto p, ProjetoRequest req) {
        p.setTitulo(req.titulo().trim());
        p.setDescricao(req.descricao().trim());
        p.setPilar(req.pilar());
        p.setEtapa(req.etapa() != null ? req.etapa() : EtapaProjeto.PLANEJAMENTO);
        p.setStatus(req.status() != null ? req.status() : StatusProjeto.EM_ANDAMENTO);
        p.setInvestimento(req.investimento());
        p.setRetornoFinanceiro(req.retornoFinanceiro());
        p.setPrazoMeses(req.prazoMeses());
        p.setAumentoProdutividade(req.aumentoProdutividade());
        p.setReducaoCustos(req.reducaoCustos());
        p.setResultados(req.resultados());

        if (req.orientacaoId() != null && !req.orientacaoId().isBlank()) {
            Orientacao o = orientacaoService.buscar(req.orientacaoId());
            p.setOrientacaoId(o.getId());
            p.setOrientacaoTitulo(o.getTitulo());
        } else {
            p.setOrientacaoId(null);
            p.setOrientacaoTitulo(null);
        }

        if (req.ideiaOrigemId() != null && !req.ideiaOrigemId().isBlank()) {
            Ideia ideia = ideiaRepository.findById(req.ideiaOrigemId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Ideia de origem nao encontrada: " + req.ideiaOrigemId()));
            p.setIdeiaOrigemId(ideia.getId());
            p.setIdeiaOrigemTitulo(ideia.getTitulo());
            // Se o projeto nao informou estrategia, herda a da ideia.
            if (p.getOrientacaoId() == null && ideia.getOrientacaoId() != null) {
                p.setOrientacaoId(ideia.getOrientacaoId());
                p.setOrientacaoTitulo(ideia.getOrientacaoTitulo());
            }
        } else {
            p.setIdeiaOrigemId(null);
            p.setIdeiaOrigemTitulo(null);
        }
    }
}
