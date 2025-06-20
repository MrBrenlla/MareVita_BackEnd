package gal.marevita.anzol.repository;

import gal.marevita.commons.repositoryEntities.CaptureRepositoryEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;


public interface CaptureRepository extends MongoRepository<CaptureRepositoryEntity, String> {

  List<CaptureRepositoryEntity> findByOwner(String owner);

}
