package br.com.fiap.inovagab.api.service;

import br.com.fiap.inovagab.api.domain.Orientacao;
import br.com.fiap.inovagab.api.domain.RegistroHistorico;
import br.com.fiap.inovagab.api.domain.Role;
import br.com.fiap.inovagab.api.domain.Usuario;
import br.com.fiap.inovagab.api.dto.OrientacaoRequest;
import br.com.fiap.inovagab.api.exception.RecursoNaoEncontradoException;
import br.com.fiap.inovagab.api.repository.OrientacaoRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class OrientacaoService {

    private final OrientacaoRepository repository;

    public OrientacaoService(OrientacaoRepository repository) {
        this.repository = repository;
    }

    /** Lideres podem ver inativas (historico); demais perfis so veem as vigentes. */
    public List<Orientacao> listar(boolean incluirInativas, Usuario solicitante) {
        boolean podeVerInativas = solicitante.getRole() == Role.LIDER && incluirInativas;
        return podeVerInativas
                ? repository.findAllByOrderByCriadoEmDesc()
                : repository.findByAtivaTrueOrderByCriadoEmDesc();
    }

    public List<Orientacao> listarVigentes() {
        return repository.findByAtivaTrueOrderByCriadoEmDesc();
    }

    public Orientacao buscar(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Orientacao nao encontrada: " + id));
    }

    public Orientacao criar(OrientacaoRequest req, Usuario lider) {
        Instant agora = Instant.now();
        Orientacao o = Orientacao.builder()
                .titulo(req.titulo().trim())
                .descricao(req.descricao().trim())
                .pilar(req.pilar())
                .categoria(req.categoria())
                .campanha(req.campanha())
                .dataInicio(req.dataInicio() != null ? req.dataInicio() : LocalDate.now())
                .dataFim(req.dataFim())
                .ativa(req.ativa() == null || req.ativa())
                .criadoPorId(lider.getId())
                .criadoPorNome(lider.getNome())
                .criadoEm(agora)
                .atualizadoEm(agora)
                .build();
        o.getHistorico().add(registro("CRIADA", o, lider, "Orientacao publicada"));
        return repository.save(o);
    }

    public Orientacao atualizar(String id, OrientacaoRequest req, Usuario lider) {
        Orientacao o = buscar(id);
        boolean estavaAtiva = o.isAtiva();

        o.setTitulo(req.titulo().trim());
        o.setDescricao(req.descricao().trim());
        o.setPilar(req.pilar());
        o.setCategoria(req.categoria());
        o.setCampanha(req.campanha());
        if (req.dataInicio() != null) o.setDataInicio(req.dataInicio());
        o.setDataFim(req.dataFim());
        if (req.ativa() != null) o.setAtiva(req.ativa());
        o.setAtualizadoEm(Instant.now());

        String acao = "ATUALIZADA";
        if (estavaAtiva && !o.isAtiva()) acao = "ENCERRADA";
        if (!estavaAtiva && o.isAtiva()) acao = "REATIVADA";
        o.getHistorico().add(registro(acao, o, lider, "Dados da orientacao alterados"));
        return repository.save(o);
    }

    /** Exclusao logica: mantem o registro historico da estrategia. */
    public Orientacao encerrar(String id, Usuario lider) {
        Orientacao o = buscar(id);
        o.setAtiva(false);
        if (o.getDataFim() == null) o.setDataFim(LocalDate.now());
        o.setAtualizadoEm(Instant.now());
        o.getHistorico().add(registro("ENCERRADA", o, lider, "Orientacao encerrada pela lideranca"));
        return repository.save(o);
    }

    public List<RegistroHistorico> historico(String id) {
        return buscar(id).getHistorico();
    }

    private RegistroHistorico registro(String acao, Orientacao o, Usuario usuario, String detalhe) {
        return RegistroHistorico.builder()
                .id(UUID.randomUUID().toString())
                .data(Instant.now())
                .acao(acao)
                .categoria(o.getCategoria())
                .campanha(o.getCampanha())
                .usuarioNome(usuario.getNome())
                .detalhe(detalhe)
                .build();
    }
}
