package br.com.fiap.inovagab.api.service;

import br.com.fiap.inovagab.api.domain.Usuario;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

/**
 * Gamificacao: toda mutacao de pontos passa pelo backend com operacoes atomicas do MongoDB ($inc),
 * eliminando a limitacao da Sprint 1 (pontos alterados a partir do cliente).
 */
@Service
public class PontosService {

    public static final int PONTOS_IDEIA_CADASTRADA = 5;
    public static final int PONTOS_IDEIA_APROVADA = 50;

    private final MongoTemplate mongoTemplate;

    public PontosService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public void creditar(String usuarioId, int pontos) {
        if (usuarioId == null || pontos <= 0) return;
        mongoTemplate.updateFirst(
                Query.query(Criteria.where("_id").is(usuarioId)),
                new Update().inc("pontos", pontos),
                Usuario.class);
    }

    /**
     * Debita pontos apenas se houver saldo suficiente (findAndModify atomico).
     * @return usuario atualizado, ou null se o saldo era insuficiente.
     */
    public Usuario debitarSeHouverSaldo(String usuarioId, int custo) {
        return mongoTemplate.findAndModify(
                Query.query(Criteria.where("_id").is(usuarioId).and("pontos").gte(custo)),
                new Update().inc("pontos", -custo),
                FindAndModifyOptions.options().returnNew(true),
                Usuario.class);
    }
}
