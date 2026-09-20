package br.com.fiap.inovagab.api.repository;

import br.com.fiap.inovagab.api.domain.Projeto;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProjetoRepository extends MongoRepository<Projeto, String> {
    List<Projeto> findAllByOrderByCriadoEmDesc();
    List<Projeto> findByOrientacaoId(String orientacaoId);
}
