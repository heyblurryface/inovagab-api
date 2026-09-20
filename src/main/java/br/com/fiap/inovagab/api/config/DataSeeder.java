package br.com.fiap.inovagab.api.config;

import br.com.fiap.inovagab.api.domain.EtapaProjeto;
import br.com.fiap.inovagab.api.domain.Ideia;
import br.com.fiap.inovagab.api.domain.Orientacao;
import br.com.fiap.inovagab.api.domain.Projeto;
import br.com.fiap.inovagab.api.domain.Recompensa;
import br.com.fiap.inovagab.api.domain.RegistroHistorico;
import br.com.fiap.inovagab.api.domain.Role;
import br.com.fiap.inovagab.api.domain.StatusIdeia;
import br.com.fiap.inovagab.api.domain.StatusProjeto;
import br.com.fiap.inovagab.api.domain.Usuario;
import br.com.fiap.inovagab.api.repository.IdeiaRepository;
import br.com.fiap.inovagab.api.repository.OrientacaoRepository;
import br.com.fiap.inovagab.api.repository.ProjetoRepository;
import br.com.fiap.inovagab.api.repository.RecompensaRepository;
import br.com.fiap.inovagab.api.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Popula o banco na primeira execucao (usuarios de teste, catalogo de recompensas e dados de exemplo).
 * Desative com SEED_ENABLED=false.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "inovagab.seed", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements CommandLineRunner {

    public static final String SENHA_PADRAO = "123456";

    private final UsuarioRepository usuarios;
    private final OrientacaoRepository orientacoes;
    private final IdeiaRepository ideias;
    private final ProjetoRepository projetos;
    private final RecompensaRepository recompensas;
    private final PasswordEncoder encoder;

    public DataSeeder(UsuarioRepository usuarios, OrientacaoRepository orientacoes, IdeiaRepository ideias,
                      ProjetoRepository projetos, RecompensaRepository recompensas, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.orientacoes = orientacoes;
        this.ideias = ideias;
        this.projetos = projetos;
        this.recompensas = recompensas;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (recompensas.count() == 0) {
            seedRecompensas();
        }
        if (usuarios.count() > 0) {
            return;
        }
        log.info("Banco vazio - executando seed inicial...");

        Instant agora = Instant.now();
        Usuario operador = usuarios.save(usuario("Carlos Operador", "operador@inovagab.com", Role.OPERADOR, agora));
        Usuario gestor = usuarios.save(usuario("Marina Gestora", "gestor@inovagab.com", Role.GESTOR, agora));
        Usuario lider = usuarios.save(usuario("Roberto Lider", "lider@inovagab.com", Role.LIDER, agora));

        Orientacao o1 = orientacoes.save(orientacao(
                "Eficiencia operacional na frota",
                "Reduzir custos de combustivel, manutencao e tempo ocioso dos veiculos por meio de dados e automacao.",
                "Gestao de Projetos", "Eficiencia", "Frota Inteligente 2026", lider, agora));
        Orientacao o2 = orientacoes.save(orientacao(
                "Experiencia do cliente digital",
                "Melhorar a jornada do passageiro nos canais digitais (app, site, atendimento) e reduzir reclamacoes.",
                "Inovacao Aberta", "Cliente", "Viaje Facil", lider, agora));

        Ideia i1 = ideias.save(Ideia.builder()
                .titulo("Checklist digital de inspecao do onibus")
                .descricao("Substituir o checklist em papel por um formulario no celular, com foto e alerta automatico para a manutencao. Reduz retrabalho, atraso na saida e falhas nao reportadas.")
                .pilar("Gestao de Projetos").orientacaoId(o1.getId()).orientacaoTitulo(o1.getTitulo())
                .autorId(operador.getId()).autorNome(operador.getNome())
                .status(StatusIdeia.APROVADA).prioridade(4).criadoEm(agora).atualizadoEm(agora).build());
        ideias.save(Ideia.builder()
                .titulo("Alerta de excesso de marcha lenta")
                .descricao("Usar a telemetria para avisar o motorista quando o veiculo ficar parado ligado por mais de 5 minutos, economizando combustivel.")
                .pilar("Mensuracao").orientacaoId(o1.getId()).orientacaoTitulo(o1.getTitulo())
                .autorId(operador.getId()).autorNome(operador.getNome())
                .status(StatusIdeia.PENDENTE).prioridade(0).criadoEm(agora).atualizadoEm(agora).build());
        ideias.save(Ideia.builder()
                .titulo("Aviso de atraso pelo WhatsApp")
                .descricao("Enviar mensagem automatica ao passageiro quando o onibus atrasar mais de 15 minutos, com nova previsao de chegada.")
                .pilar("Inovacao Aberta").orientacaoId(o2.getId()).orientacaoTitulo(o2.getTitulo())
                .autorId(operador.getId()).autorNome(operador.getNome())
                .status(StatusIdeia.PENDENTE).prioridade(0).criadoEm(agora).atualizadoEm(agora).build());

        // pontos: 3 ideias (+15) e 1 aprovada (+50)
        operador.setPontos(65);
        usuarios.save(operador);

        projetos.save(projeto("Checklist digital de inspecao", "Implantacao do checklist digital em 40 garagens.",
                "Gestao de Projetos", o1, i1, gestor, EtapaProjeto.EXECUCAO, StatusProjeto.EM_ANDAMENTO,
                120_000, 210_000, 6, 12.5, 8.0, "Piloto em 5 garagens concluido com 30% menos falhas nao reportadas.", agora));
        projetos.save(projeto("Telemetria de consumo", "Sensores e painel de consumo de combustivel por rota.",
                "Mensuracao", o1, null, gestor, EtapaProjeto.CONCLUIDO, StatusProjeto.CONCLUIDO,
                300_000, 520_000, 9, 6.0, 11.0, "Economia de 9% no combustivel das rotas monitoradas.", agora));
        projetos.save(projeto("Novo app do passageiro", "Redesenho do app com compra, check-in e rastreamento.",
                "Inovacao Aberta", o2, null, gestor, EtapaProjeto.PLANEJAMENTO, StatusProjeto.EM_ANDAMENTO,
                450_000, 0, 12, 0.0, 0.0, null, agora));

        log.info("Seed concluido. Usuarios de teste (senha '{}'): operador@inovagab.com, gestor@inovagab.com, lider@inovagab.com",
                SENHA_PADRAO);
    }

    private Usuario usuario(String nome, String email, Role role, Instant agora) {
        return Usuario.builder().nome(nome).email(email).senhaHash(encoder.encode(SENHA_PADRAO))
                .role(role).pontos(0).ativo(true).criadoEm(agora).build();
    }

    private Orientacao orientacao(String titulo, String descricao, String pilar, String categoria,
                                  String campanha, Usuario lider, Instant agora) {
        Orientacao o = Orientacao.builder().titulo(titulo).descricao(descricao).pilar(pilar)
                .categoria(categoria).campanha(campanha).dataInicio(LocalDate.now().withDayOfMonth(1))
                .ativa(true).criadoPorId(lider.getId()).criadoPorNome(lider.getNome())
                .criadoEm(agora).atualizadoEm(agora).build();
        o.getHistorico().add(RegistroHistorico.builder().id(UUID.randomUUID().toString()).data(agora)
                .acao("CRIADA").categoria(categoria).campanha(campanha).usuarioNome(lider.getNome())
                .detalhe("Orientacao publicada (seed)").build());
        return o;
    }

    private Projeto projeto(String titulo, String descricao, String pilar, Orientacao o, Ideia ideia, Usuario gestor,
                            EtapaProjeto etapa, StatusProjeto status, double invest, double retorno, int prazo,
                            double produtividade, double reducaoCustos, String resultados, Instant agora) {
        return Projeto.builder().titulo(titulo).descricao(descricao).pilar(pilar)
                .orientacaoId(o.getId()).orientacaoTitulo(o.getTitulo())
                .ideiaOrigemId(ideia != null ? ideia.getId() : null)
                .ideiaOrigemTitulo(ideia != null ? ideia.getTitulo() : null)
                .gestorId(gestor.getId()).gestorNome(gestor.getNome())
                .etapa(etapa).status(status).investimento(invest).retornoFinanceiro(retorno).prazoMeses(prazo)
                .aumentoProdutividade(produtividade).reducaoCustos(reducaoCustos).resultados(resultados)
                .criadoEm(agora).atualizadoEm(agora).build();
    }

    private void seedRecompensas() {
        recompensas.saveAll(List.of(
                rec("Vale-combustivel R$ 50", "Credito para abastecimento em postos parceiros", 100, "⛽", "Mobilidade"),
                rec("Vale-refeicao R$ 30", "Credito para alimentacao no horario de almoco", 80, "🍔", "Alimentacao"),
                rec("Estacionamento premium", "Vaga premium por 1 mes", 150, "🚗", "Mobilidade"),
                rec("Kit Aguia Branca", "Camiseta + caneca + bloco exclusivos da marca", 200, "🎁", "Brindes"),
                rec("Voucher curso EAD", "Acesso a curso de desenvolvimento profissional", 300, "📚", "Desenvolvimento"),
                rec("Dia de folga", "Folga adicional para usar quando quiser", 500, "🌴", "Bem-estar")));
    }

    private Recompensa rec(String titulo, String descricao, int custo, String emoji, String categoria) {
        return Recompensa.builder().titulo(titulo).descricao(descricao).custo(custo).emoji(emoji)
                .categoria(categoria).disponivel(true).build();
    }
}
