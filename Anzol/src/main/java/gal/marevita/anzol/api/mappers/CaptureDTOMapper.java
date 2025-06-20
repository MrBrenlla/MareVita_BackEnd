package gal.marevita.anzol.api.mappers;

import gal.marevita.anzol.apigenerator.openapi.api.model.CaptureDTO;
import gal.marevita.anzol.apigenerator.openapi.api.model.GPSLocationDTO;
import gal.marevita.anzol.model.Capture;
import gal.marevita.anzol.model.GPSLocation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.time.ZonedDateTime;

@Mapper
public interface CaptureDTOMapper {

  CaptureDTOMapper INSTANCE = Mappers.getMapper(CaptureDTOMapper.class);

  @Mapping(target = "dateTime", source = "dateTime", qualifiedByName = "zonedDateTimeToString")
  @Mapping(target = "gpsLocation", source = "gpsLocation", qualifiedByName = "gpsToGpsDTO")
  CaptureDTO CaptureToCaptureDTO(Capture capture);

  @Mapping(target = "dateTime", source = "dateTime", qualifiedByName = "stringToZonedDateTime")
  @Mapping(target = "gpsLocation", source = "gpsLocation", qualifiedByName = "gpsDTOToGps")
  Capture CaptureDTOToCapture(CaptureDTO dto);

  @Named("zonedDateTimeToString")
  default String zonedDateTimeToString(ZonedDateTime time) {
    return time != null ? time.toString() : null;
  }

  @Named("stringToZonedDateTime")
  default ZonedDateTime stringToZonedDateTime(String time) {
    return time != null ? ZonedDateTime.parse(time) : null;
  }

  @Named("gpsToGpsDTO")
  default GPSLocationDTO gpsToGpsDTO(GPSLocation gps) {
    if (gps == null) return null;
    return GPSLocationDTO.builder()
        .latitude(gps.latitude() != null ? gps.latitude().floatValue() : null)
        .longitude(gps.longitude() != null ? gps.longitude().floatValue() : null)
        .build();
  }

  @Named("gpsDTOToGps")
  default GPSLocation gpsDTOToGps(GPSLocationDTO dto) {
    if (dto == null) return null;
    return GPSLocation.builder()
        .latitude(dto.getLatitude() != null ? dto.getLatitude().doubleValue() : null)
        .longitude(dto.getLongitude() != null ? dto.getLongitude().doubleValue() : null)
        .build();
  }
}