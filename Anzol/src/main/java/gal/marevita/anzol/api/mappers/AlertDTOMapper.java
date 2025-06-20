package gal.marevita.anzol.api.mappers;

import gal.marevita.anzol.apigenerator.openapi.api.model.AlertDTO;
import gal.marevita.anzol.apigenerator.openapi.api.model.GPSLocationDTO;
import gal.marevita.anzol.model.Alert;
import gal.marevita.anzol.model.GPSLocation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AlertDTOMapper {

  AlertDTOMapper INSTANCE = Mappers.getMapper(AlertDTOMapper.class);

  @Mapping(target = "gpsLocation", source = "gpsLocation", qualifiedByName = "gpsToGpsDTO")
  AlertDTO AlertToAlertDTO(Alert capture);


  @Mapping(target = "gpsLocation", source = "gpsLocation", qualifiedByName = "gpsDTOToGps")
  Alert AlertDTOToAlert(AlertDTO dto);

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