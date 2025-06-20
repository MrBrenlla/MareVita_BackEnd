package gal.marevita.anzol.repository;

import gal.marevita.commons.repositoryEntities.LatestCapturesRepositoryEntity;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface LatestCapturesRepository extends MongoRepository<LatestCapturesRepositoryEntity, String> {

}
