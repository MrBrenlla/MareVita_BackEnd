package gal.marevita.cana.api.mappers;

import gal.marevita.cana.apigenerator.openapi.api.model.UserDTO;
import gal.marevita.cana.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserDTOMapper {

  UserDTOMapper INSTANCE = Mappers.getMapper(UserDTOMapper.class);

  User UserDTOToUser(UserDTO userDTO);

  UserDTO UserToUserDTO(User user);


}