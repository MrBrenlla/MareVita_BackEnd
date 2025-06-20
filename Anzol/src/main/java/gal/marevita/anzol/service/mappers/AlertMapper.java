package gal.marevita.anzol.service.mappers;

import gal.marevita.anzol.model.Alert;
import gal.marevita.anzol.model.Fish;
import gal.marevita.anzol.model.GPSLocation;
import gal.marevita.anzol.model.WeatherCondition;
import gal.marevita.commons.repositoryEntities.AlertRepositoryEntity;
import gal.marevita.commons.repositoryEntities.FishRepositoryEntity;
import gal.marevita.commons.repositoryEntities.GPSLocationRepositoryEntity;
import gal.marevita.commons.repositoryEntities.WeatherConditionRepositoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AlertMapper {

  AlertMapper INSTANCE = Mappers.getMapper(AlertMapper.class);

  Alert AlertRepositoryEntityToAlert(AlertRepositoryEntity entity);

  AlertRepositoryEntity AlertToAlertRepositoryEntity(Alert model);

  GPSLocation GPSLocationRepositoryEntityToGPSLocation(GPSLocationRepositoryEntity entity);

  GPSLocationRepositoryEntity GPSLocationToGPSLocationRepositoryEntity(GPSLocation model);

  WeatherCondition WeatherConditionRepositoryEntityToWeatherCondition(WeatherConditionRepositoryEntity entity);

  WeatherConditionRepositoryEntity WeatherConditionToWeatherConditionRepositoryEntity(WeatherCondition model);

  Fish FishRepositoryEntityToFish(FishRepositoryEntity entity);

  FishRepositoryEntity FishToFishRepositoryEntity(Fish model);
}