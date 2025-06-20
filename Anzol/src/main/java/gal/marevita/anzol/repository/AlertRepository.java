package gal.marevita.anzol.repository;

import gal.marevita.commons.repositoryEntities.AlertRepositoryEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AlertRepository extends MongoRepository<AlertRepositoryEntity, String> {

  List<AlertRepositoryEntity> findByOwner(String owner);

}
