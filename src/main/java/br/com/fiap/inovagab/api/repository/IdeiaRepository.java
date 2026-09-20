package br.com.fiap.inovagab.api.repository;

import br.com.fiap.inovagab.api.domain.Ideia;
import br.com.fiap.inovagab.api.domain.StatusIdeia;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface IdeiaRepository extends MongoRepository<Ideia, String> {
    List<Ideia> findByAutorIdOrderByCriadoEmDesc(String autorId);
    List<Ideia> findAllByOrderByCriadoEmDesc();
    List<Ideia> findByStatusOrderByCriadoEmDesc(StatusIdeia status);
    List<Ideia> findByOrientacaoId(String orientacaoId);
    List<Ideia> findByStatusAndAvaliacaoIaIsNull(StatusIdeia status);
    List<Ideia> findByAvaliacaoIaIsNotNull();
    long countByStatus(StatusIdeia status);
}
