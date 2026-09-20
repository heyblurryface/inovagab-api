package br.com.fiap.inovagab.api.repository;

import br.com.fiap.inovagab.api.domain.Resgate;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ResgateRepository extends MongoRepository<Resgate, String> {
    List<Resgate> findByOperadorIdOrderByResgatadoEmDesc(String operadorId);
}
