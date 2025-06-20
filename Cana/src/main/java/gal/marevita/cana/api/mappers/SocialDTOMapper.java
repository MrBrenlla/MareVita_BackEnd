package gal.marevita.cana.api.mappers;

import gal.marevita.cana.apigenerator.openapi.api.model.SocialDTO;
import gal.marevita.cana.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SocialDTOMapper {

  SocialDTOMapper INSTANCE = Mappers.getMapper(SocialDTOMapper.class);

  SocialDTO UserToSocialDTO(User user);

}