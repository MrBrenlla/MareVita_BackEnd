package gal.marevita.anzol.api.mappers;

import gal.marevita.anzol.apigenerator.openapi.api.model.CaptureInfoDTO;
import gal.marevita.anzol.model.Capture;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.time.ZonedDateTime;
import java.util.List;

@Mapper
public interface CaptureInfoDTOMapper {

  CaptureInfoDTOMapper INSTANCE = Mappers.getMapper(CaptureInfoDTOMapper.class);

  List<CaptureInfoDTO> CaptureToCaptureInfoDTO(List<Capture> capture);

  @Mapping(target = "dateTime", source = "dateTime", qualifiedByName = "zonedDateTimeToString")
  CaptureInfoDTO CaptureToCaptureInfoDTO(Capture capture);

  @Mapping(target = "dateTime", source = "dateTime", qualifiedByName = "stringToZonedDateTime")
  Capture CaptureInfoDTOToCapture(CaptureInfoDTO dto);

  @Named("zonedDateTimeToString")
  default String zonedDateTimeToString(ZonedDateTime time) {
    return time != null ? time.toString() : null;
  }

  @Named("stringToZonedDateTime")
  default ZonedDateTime stringToZonedDateTime(String time) {
    return time != null ? ZonedDateTime.parse(time) : null;
  }


}