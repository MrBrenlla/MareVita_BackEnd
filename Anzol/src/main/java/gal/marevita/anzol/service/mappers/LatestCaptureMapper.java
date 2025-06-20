package gal.marevita.anzol.service.mappers;

import gal.marevita.anzol.model.LatestCapture;
import gal.marevita.commons.repositoryEntities.LatestCapturesRepositoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface LatestCaptureMapper {

  LatestCaptureMapper INSTANCE = Mappers.getMapper(LatestCaptureMapper.class);

  LatestCapture LatestCaptureRepositoryEntityToLatestCapture(LatestCapturesRepositoryEntity latestCaptureRepositoryEntity);

  LatestCapturesRepositoryEntity LatestCaptureToLatestCaptureRepositoryEntity(LatestCapture latestCapture);


}