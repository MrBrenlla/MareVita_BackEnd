package gal.marevita.cana.api.mappers;


import gal.marevita.cana.apigenerator.openapi.api.model.UserDataDTO;
import gal.marevita.cana.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserDataDTOMapper {

  UserDataDTOMapper INSTANCE = Mappers.getMapper(UserDataDTOMapper.class);

  @Mapping(target = "friendsCount", expression = "java(user.friends() != null ? user.friends().size() : 0)")
  UserDataDTO UserToUserDataDTO(User user);

  User UserDataDTOToUser(UserDataDTO userDataDTO);
}
