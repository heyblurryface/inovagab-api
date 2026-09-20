package br.com.fiap.inovagab.api.repository;

import br.com.fiap.inovagab.api.domain.Recompensa;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RecompensaRepository extends MongoRepository<Recompensa, String> {
    List<Recompensa> findByDisponivelTrueOrderByCustoAsc();
}
