package gal.marevita.minhoca.service.mappers;

import gal.marevita.minhoca.model.Capture;
import gal.marevita.minhoca.model.statistics.CaptureInfo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;


@Mapper
public interface CaptureInfoMapper {

  CaptureInfoMapper INSTANCE = Mappers.getMapper(CaptureInfoMapper.class);

  CaptureInfo toCaptureInfo(Capture capture);
}