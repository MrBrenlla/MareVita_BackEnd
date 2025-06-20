package gal.marevita.minhoca.api;

import gal.marevita.minhoca.api.mappers.LatestCaptureDTOMapper;
import gal.marevita.minhoca.apigenerator.openapi.api.LatestCapturesApi;
import gal.marevita.minhoca.service.LatestCapturesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LatestCapturesApiImp implements LatestCapturesApi {

  @Autowired
  private LatestCapturesService latestCapturesService;

  @Override
  public ResponseEntity latestCaptures() {
    try {
      return ResponseEntity.ok(LatestCaptureDTOMapper.INSTANCE.LatestCaptureToLatestCaptureDTO(
          latestCapturesService.getLatestCaptures()));
    } catch (Exception e) {
      return ExceptionManager.manage(e);
    }
  }

}
