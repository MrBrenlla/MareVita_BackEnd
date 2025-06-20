package gal.marevita.anzol.api;

import gal.marevita.anzol.api.mappers.AlertDTOMapper;
import gal.marevita.anzol.api.mappers.PeriodDTOMapper;
import gal.marevita.anzol.apigenerator.openapi.api.AlertApi;
import gal.marevita.anzol.apigenerator.openapi.api.model.AlertDTO;
import gal.marevita.anzol.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AlertApiImp implements AlertApi {

  @Autowired
  private AlertService alertService;


  @Override
  public ResponseEntity newAlert(AlertDTO alertDTO) {

    try {
      AlertDTO alert = AlertDTOMapper.INSTANCE.AlertToAlertDTO(
          alertService.newAlert(AlertDTOMapper.INSTANCE.AlertDTOToAlert(alertDTO)));
      alert.setActivated(PeriodDTOMapper.INSTANCE.toPeriodDTOList(alertService.checkAlert(alert.getId())));
      return ResponseEntity.ok(alert);
    } catch (Exception e) {
      return ExceptionManager.manage(e);
    }

  }

  @Override
  public ResponseEntity updateAlert(String alertId, AlertDTO alertDTO) {
    try {
      AlertDTO alert = AlertDTOMapper.INSTANCE.AlertToAlertDTO(
          alertService.updateAlert(alertId, AlertDTOMapper.INSTANCE.AlertDTOToAlert(alertDTO)));
      alert.setActivated(PeriodDTOMapper.INSTANCE.toPeriodDTOList(alertService.checkAlert(alert.getId())));
      return ResponseEntity.ok(alert);
    } catch (Exception e) {
      return ExceptionManager.manage(e);
    }
  }

  @Override
  public ResponseEntity deleteAlert(String alertId) {
    try {
      alertService.deleteAlert(alertId);
      return ResponseEntity.ok().build();
    } catch (Exception e) {
      return ExceptionManager.manage(e);
    }
  }

  @Override
  public ResponseEntity getAlerts() {
    try {

      List<AlertDTO> alerts = alertService.getAlerts().stream()
          .map(AlertDTOMapper.INSTANCE::AlertToAlertDTO)
          .toList();
      for (AlertDTO alert : alerts) {
        try {
          alert.setActivated(PeriodDTOMapper.INSTANCE.toPeriodDTOList(alertService.checkAlert(alert.getId())));
        } catch (Exception ignored) {
          alert.setActivated(List.of());
        }
      }
      return ResponseEntity.ok(alerts);
    } catch (Exception e) {
      return ExceptionManager.manage(e);
    }
  }

  @Override
  public ResponseEntity getAlert(String alertId) {
    try {
      AlertDTO alert = AlertDTOMapper.INSTANCE.AlertToAlertDTO(
          alertService.getAlert(alertId));
      alert.setActivated(PeriodDTOMapper.INSTANCE.toPeriodDTOList(alertService.checkAlert(alert.getId())));
      return ResponseEntity.ok(alert);
    } catch (Exception e) {
      return ExceptionManager.manage(e);
    }
  }

  @Override
  public ResponseEntity checkAlert(String alertId) {
    try {
      return ResponseEntity.ok(PeriodDTOMapper.INSTANCE.toPeriodDTOList(
          alertService.checkAlert(alertId))
      );
    } catch (Exception e) {
      return ExceptionManager.manage(e);
    }
  }

  @Override
  public ResponseEntity checkAlerts() {
    try {
      return ResponseEntity.ok(PeriodDTOMapper.INSTANCE.toPeriodDTOList(
          alertService.checkAlerts())
      );
    } catch (Exception e) {
      return ExceptionManager.manage(e);
    }
  }

}
