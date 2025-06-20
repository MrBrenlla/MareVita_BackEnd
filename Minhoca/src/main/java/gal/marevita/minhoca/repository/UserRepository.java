package gal.marevita.minhoca.repository;

import gal.marevita.commons.repositoryEntities.UserRepositoryEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;


public interface UserRepository extends MongoRepository<UserRepositoryEntity, String> {


  Optional<UserRepositoryEntity> findByUserName(String userName);

}
