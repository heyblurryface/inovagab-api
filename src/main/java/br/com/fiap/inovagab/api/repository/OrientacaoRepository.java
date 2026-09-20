package br.com.fiap.inovagab.api.repository;

import br.com.fiap.inovagab.api.domain.Orientacao;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OrientacaoRepository extends MongoRepository<Orientacao, String> {
    List<Orientacao> findByAtivaTrueOrderByCriadoEmDesc();
    List<Orientacao> findAllByOrderByCriadoEmDesc();
}
