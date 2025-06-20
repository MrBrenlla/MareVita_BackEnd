package gal.marevita.minhoca.service.mappers;

import gal.marevita.commons.repositoryEntities.UserRepositoryEntity;
import gal.marevita.minhoca.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Set;

@Mapper
public interface UserRepositoryEntityMapper {

  UserRepositoryEntityMapper INSTANCE = Mappers.getMapper(UserRepositoryEntityMapper.class);

  User UserRepositoryEntityToUser(UserRepositoryEntity userRepositoryEntity);

  Set<User> UserRepositoryEntityToUser(Set<UserRepositoryEntity> userRepositoryEntity);

  UserRepositoryEntity UserToUserRepositoryEntity(User user);


}