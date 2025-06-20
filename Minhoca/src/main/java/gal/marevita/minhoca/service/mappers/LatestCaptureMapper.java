package gal.marevita.minhoca.service.mappers;

import gal.marevita.commons.repositoryEntities.LatestCapturesRepositoryEntity;
import gal.marevita.minhoca.model.LatestCapture;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Mapper
public interface LatestCaptureMapper {

  LatestCaptureMapper INSTANCE = Mappers.getMapper(LatestCaptureMapper.class);

  @Mapping(source = "dateTime", target = "dateTime", qualifiedByName = "mapInstantToZonedDateTime")
  LatestCapture LatestCaptureRepositoryEntityToLatestCapture(LatestCapturesRepositoryEntity entity);

  List<LatestCapture> LatestCaptureRepositoryEntityToLatestCapture(List<LatestCapturesRepositoryEntity> entities);

  @Named("mapInstantToZonedDateTime")
  default ZonedDateTime mapInstantToZonedDateTime(Instant instant) {
    return instant != null ? instant.atZone(ZoneId.of("Europe/Madrid")) : null;
  }
}