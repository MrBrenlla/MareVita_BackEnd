package gal.marevita.minhoca.api;

import gal.marevita.minhoca.api.mappers.StatisticsDTOMapper;
import gal.marevita.minhoca.apigenerator.openapi.api.StatisticsApi;
import gal.marevita.minhoca.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatisticsApiImp implements StatisticsApi {

  @Autowired
  private StatisticsService statisticsService;

  @Override
  public ResponseEntity getStatistics(String userName) {
    try {
      return ResponseEntity.ok(StatisticsDTOMapper.INSTANCE.toStatisticsDTO(
          statisticsService.getStatistics(userName)));
    } catch (Exception e) {
      return ExceptionManager.manage(e);
    }
  }

}
