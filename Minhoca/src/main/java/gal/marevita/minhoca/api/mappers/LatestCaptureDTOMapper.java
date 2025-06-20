package gal.marevita.minhoca.api.mappers;

import gal.marevita.minhoca.apigenerator.openapi.api.model.LatestCaptureDTO;
import gal.marevita.minhoca.model.LatestCapture;
import gal.marevita.minhoca.util.DateTimeConverter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.time.ZonedDateTime;
import java.util.List;

@Mapper(uses = DateTimeConverter.class)
public interface LatestCaptureDTOMapper {

  LatestCaptureDTOMapper INSTANCE = Mappers.getMapper(LatestCaptureDTOMapper.class);

  @Mapping(target = "dateTime", source = "dateTime", qualifiedByName = "zonedDateTimeToString")
  LatestCaptureDTO LatestCaptureToLatestCaptureDTO(LatestCapture latestCapture);

  List<LatestCaptureDTO> LatestCaptureToLatestCaptureDTO(List<LatestCapture> latestCapture);

  @Mapping(target = "dateTime", source = "dateTime", qualifiedByName = "stringToZonedDateTime")
  LatestCapture LatestCaptureDTOToLatestCapture(LatestCaptureDTO latestCaptureDTO);

  @Named("zonedDateTimeToString")
  default String zonedDateTimeToString(ZonedDateTime time) {
    return time != null ? time.toString() : null;
  }

  @Named("stringToZonedDateTime")
  default ZonedDateTime stringToZonedDateTime(String time) {
    return time != null ? ZonedDateTime.parse(time) : null;
  }
}