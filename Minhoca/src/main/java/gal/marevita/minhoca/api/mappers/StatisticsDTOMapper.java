package gal.marevita.minhoca.api.mappers;

import gal.marevita.minhoca.apigenerator.openapi.api.model.CaptureInfoDTO;
import gal.marevita.minhoca.apigenerator.openapi.api.model.StatisticsDTO;
import gal.marevita.minhoca.model.statistics.CaptureInfo;
import gal.marevita.minhoca.model.statistics.Statistics;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.ZonedDateTime;

@Mapper
public interface StatisticsDTOMapper {

  StatisticsDTOMapper INSTANCE = org.mapstruct.factory.Mappers.getMapper(StatisticsDTOMapper.class);

  @Mapping(target = "dateTime", source = "dateTime", qualifiedByName = "zonedDateTimeToString")
  CaptureInfoDTO toCaptureInfoDTO(CaptureInfo captureInfo);

  StatisticsDTO toStatisticsDTO(Statistics statistics);


  @Named("zonedDateTimeToString")
  default String zonedDateTimeToString(ZonedDateTime time) {
    return time != null ? time.toString() : null;
  }

  @Named("stringToZonedDateTime")
  default ZonedDateTime stringToZonedDateTime(String time) {
    return time != null ? ZonedDateTime.parse(time) : null;
  }

}
