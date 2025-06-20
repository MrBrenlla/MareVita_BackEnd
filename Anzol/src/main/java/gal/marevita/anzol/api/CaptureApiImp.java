package gal.marevita.anzol.api;

import gal.marevita.anzol.api.mappers.CaptureDTOMapper;
import gal.marevita.anzol.api.mappers.CaptureInfoDTOMapper;
import gal.marevita.anzol.apigenerator.openapi.api.CaptureApi;
import gal.marevita.anzol.apigenerator.openapi.api.model.CaptureDTO;
import gal.marevita.anzol.apigenerator.openapi.api.model.ListDTO;
import gal.marevita.anzol.service.CapturesService;
import gal.marevita.anzol.util.BaitEnum;
import gal.marevita.anzol.util.FishEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CaptureApiImp implements CaptureApi {

  @Autowired
  private CapturesService capturesService;

  @Override
  public ResponseEntity getFishes() {
    return ResponseEntity.ok(ListDTO.builder().list(FishEnum.getAllFishList()).build());
  }

  @Override
  public ResponseEntity getBaits() {
    return ResponseEntity.ok(ListDTO.builder().list(BaitEnum.getAllBaitList()).build());
  }

  @Override
  public ResponseEntity newCapture(CaptureDTO captureDTO) {
    try {
      return ResponseEntity.ok(CaptureDTOMapper.INSTANCE.CaptureToCaptureDTO(
          capturesService.saveCapture(CaptureDTOMapper.INSTANCE.CaptureDTOToCapture(captureDTO)
          )));
    } catch (Exception e) {
      return ExceptionManager.manage(e);
    }
  }

  @Override
  public ResponseEntity getCaptures() {
    try {
      return ResponseEntity.ok(CaptureInfoDTOMapper.INSTANCE.CaptureToCaptureInfoDTO(
          capturesService.getCaptures()
      ));
    } catch (Exception e) {
      return ExceptionManager.manage(e);
    }
  }

  @Override
  public ResponseEntity getFriendCaptures() {
    try {
      return ResponseEntity.ok(CaptureInfoDTOMapper.INSTANCE.CaptureToCaptureInfoDTO(
          capturesService.getFriendCaptures()
      ));
    } catch (Exception e) {
      return ExceptionManager.manage(e);
    }
  }

  @Override
  public ResponseEntity getFullCapture(String capturaId) {
    try {
      return ResponseEntity.ok(CaptureDTOMapper.INSTANCE.CaptureToCaptureDTO(
          capturesService.getCapture(capturaId)
      ));
    } catch (Exception e) {
      return ExceptionManager.manage(e);
    }
  }


  @Override
  public ResponseEntity getUserCaptures(String userName) {
    try {
      return ResponseEntity.ok(CaptureInfoDTOMapper.INSTANCE.CaptureToCaptureInfoDTO(
          capturesService.getUserCaptures(userName)));
    } catch (Exception e) {
      return ExceptionManager.manage(e);
    }
  }

  @Override
  public ResponseEntity deleteCapture(String captureId) {
    try {
      capturesService.deleteCapture(captureId);
      return ResponseEntity.ok().build();
    } catch (Exception e) {
      return ExceptionManager.manage(e);
    }
  }

  @Override
  public ResponseEntity toggleLike(String capturaId) {
    try {
      return ResponseEntity.ok(CaptureDTOMapper.INSTANCE.CaptureToCaptureDTO(
          capturesService.toggleLike(capturaId)
      ));
    } catch (Exception e) {
      return ExceptionManager.manage(e);
    }
  }


}
