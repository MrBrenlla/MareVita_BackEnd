package gal.marevita.cana.repository;

import gal.marevita.commons.repositoryEntities.UserRepositoryEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.Set;

public interface UserRepository extends MongoRepository<UserRepositoryEntity, String> {


  Optional<UserRepositoryEntity> findByUserName(String userName);

  Optional<UserRepositoryEntity> findByUserNameOrEmail(String userName, String Email);


  boolean existsByEmail(String email);

  boolean existsByUserName(String userName);

  Set<UserRepositoryEntity> findByUserNameContainingIgnoreCase(String userName);

}
