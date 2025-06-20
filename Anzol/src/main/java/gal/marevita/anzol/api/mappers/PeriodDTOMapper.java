package gal.marevita.anzol.api.mappers;

import gal.marevita.anzol.apigenerator.openapi.api.model.PeriodDTO;
import gal.marevita.anzol.model.Period;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.ZonedDateTime;
import java.util.List;

@Mapper
public interface PeriodDTOMapper {

  PeriodDTOMapper INSTANCE = org.mapstruct.factory.Mappers.getMapper(PeriodDTOMapper.class);

  @Mapping(target = "startDate", source = "startDate", qualifiedByName = "zonedDateTimeToString")
  @Mapping(target = "endDate", source = "endDate", qualifiedByName = "zonedDateTimeToString")
  List<PeriodDTO> toPeriodDTOList(List<Period> periods);


  @Named("zonedDateTimeToString")
  default String zonedDateTimeToString(ZonedDateTime time) {
    return time != null ? time.toString() : null;
  }

}
