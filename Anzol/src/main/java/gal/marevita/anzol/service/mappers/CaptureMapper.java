package gal.marevita.anzol.service.mappers;

import gal.marevita.anzol.model.Capture;
import gal.marevita.anzol.model.Fish;
import gal.marevita.anzol.model.GPSLocation;
import gal.marevita.anzol.model.WeatherCondition;
import gal.marevita.commons.repositoryEntities.CaptureRepositoryEntity;
import gal.marevita.commons.repositoryEntities.FishRepositoryEntity;
import gal.marevita.commons.repositoryEntities.GPSLocationRepositoryEntity;
import gal.marevita.commons.repositoryEntities.WeatherConditionRepositoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.time.ZonedDateTime;

@Mapper
public interface CaptureMapper {

  CaptureMapper INSTANCE = Mappers.getMapper(CaptureMapper.class);

  @Mapping(target = "dateTime", source = "dateTime", qualifiedByName = "stringToZonedDateTime")
  Capture CaptureRepositoryEntityToCapture(CaptureRepositoryEntity entity);

  @Mapping(target = "dateTime", source = "dateTime", qualifiedByName = "zonedDateTimeToString")
  CaptureRepositoryEntity CaptureToCaptureRepositoryEntity(Capture model);

  GPSLocation GPSLocationRepositoryEntityToGPSLocation(GPSLocationRepositoryEntity entity);

  GPSLocationRepositoryEntity GPSLocationToGPSLocationRepositoryEntity(GPSLocation model);

  WeatherCondition WeatherConditionRepositoryEntityToWeatherCondition(WeatherConditionRepositoryEntity entity);

  WeatherConditionRepositoryEntity WeatherConditionToWeatherConditionRepositoryEntity(WeatherCondition model);

  Fish FishRepositoryEntityToFish(FishRepositoryEntity entity);

  FishRepositoryEntity FishToFishRepositoryEntity(Fish model);

  @Named("zonedDateTimeToString")
  default String zonedDateTimeToString(ZonedDateTime time) {
    return time != null ? time.toString() : null;
  }

  @Named("stringToZonedDateTime")
  default ZonedDateTime stringToZonedDateTime(String time) {
    return time != null ? ZonedDateTime.parse(time) : null;
  }
}