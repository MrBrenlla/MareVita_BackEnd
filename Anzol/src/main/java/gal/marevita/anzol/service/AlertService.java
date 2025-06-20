package gal.marevita.anzol.service;

import gal.marevita.anzol.model.Alert;
import gal.marevita.anzol.model.Period;
import gal.marevita.anzol.model.User;
import gal.marevita.anzol.repository.AlertRepository;
import gal.marevita.anzol.repository.CaptureRepository;
import gal.marevita.anzol.repository.UserRepository;
import gal.marevita.anzol.service.exceptions.NotAccesible;
import gal.marevita.anzol.service.exceptions.WrongToken;
import gal.marevita.anzol.service.mappers.AlertMapper;
import gal.marevita.anzol.service.mappers.CaptureMapper;
import gal.marevita.anzol.service.mappers.UserRepositoryEntityMapper;
import gal.marevita.anzol.util.GPStoName;
import gal.marevita.anzol.util.WeatherChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.management.InstanceNotFoundException;
import java.util.ArrayList;
import java.util.List;

import static gal.marevita.anzol.security.JwtUtil.getActualUserId;

@Component
public class AlertService {

  @Autowired
  AlertRepository alertRepository;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private CaptureRepository captureRepository;
  @Autowired
  private WeatherChecker weatherChecker;
  @Autowired
  private GPStoName gpstoName;

  public Alert newAlert(Alert alert) {
    User user = UserRepositoryEntityMapper.INSTANCE.UserRepositoryEntityToUser(
        userRepository.findById(getActualUserId())
            .orElseThrow(() -> new WrongToken("Token Incorrecto")));

    String c = null;

    if (alert.relatedCapture() != null && !alert.relatedCapture().isBlank())
      try {
        c =
            CaptureMapper.INSTANCE.CaptureRepositoryEntityToCapture(
                    captureRepository.findById(alert.relatedCapture())
                        .orElseThrow(() -> new InstanceNotFoundException("Capture inexistente")))
                .id();
      } catch (InstanceNotFoundException ignored) {
      }

    Alert newAlert = Alert.builder()
        .owner(user.id())
        .name(alert.name())
        .relatedCapture(c)
        .gpsLocation(alert.gpsLocation())
        .location(gpstoName.obterNomeLugar(
            alert.gpsLocation().latitude(),
            alert.gpsLocation().longitude()
        ))
        .baits(alert.baits())
        .fish(alert.fish())
        .weatherConditions(alert
            .weatherConditions().stream()
            .filter(wc -> wc.value() != null && wc.error() != null)
            .toList()
        )
        .build();

    return AlertMapper.INSTANCE.AlertRepositoryEntityToAlert(alertRepository.save(
        AlertMapper.INSTANCE.AlertToAlertRepositoryEntity(newAlert)
    ));
  }


  public Alert updateAlert(String alertId, Alert updatedAlert) throws InstanceNotFoundException {
    User user = UserRepositoryEntityMapper.INSTANCE.UserRepositoryEntityToUser(
        userRepository.findById(getActualUserId())
            .orElseThrow(() -> new WrongToken("Token Incorrecto")));

    Alert existingAlert = AlertMapper.INSTANCE.AlertRepositoryEntityToAlert(
        alertRepository.findById(alertId)
            .orElseThrow(() -> new InstanceNotFoundException("Alerta inexistente"))
    );

    if (!existingAlert.owner().equals(user.id())) {
      throw new NotAccesible("Non se poden modificar alertas axenas");
    }

    String relatedCaptureId = null;
    if (updatedAlert.relatedCapture() != null && !updatedAlert.relatedCapture().isBlank())
      try {
        relatedCaptureId = CaptureMapper.INSTANCE.CaptureRepositoryEntityToCapture(
                captureRepository.findById(updatedAlert.relatedCapture())
                    .orElseThrow(() -> new InstanceNotFoundException("Captura inexistente")))
            .id();
      } catch (InstanceNotFoundException ignored) {
      }

    Alert.AlertBuilder alertToSave = existingAlert.toBuilder()
        .name(updatedAlert.name())
        .relatedCapture(relatedCaptureId)
        .clearBaits()
        .clearFish()
        .clearWeatherConditions()
        .baits(updatedAlert.baits())
        .fish(updatedAlert.fish())
        .weatherConditions(updatedAlert
            .weatherConditions().stream()
            .filter(wc -> wc.value() != null && wc.error() != null)
            .toList()
        );

    if (!existingAlert.gpsLocation().equals(updatedAlert.gpsLocation()))
      alertToSave
          .gpsLocation(updatedAlert.gpsLocation())
          .location(gpstoName.obterNomeLugar(
              updatedAlert.gpsLocation().latitude(),
              updatedAlert.gpsLocation().longitude()
          ));

    return AlertMapper.INSTANCE.AlertRepositoryEntityToAlert(
        alertRepository.save(AlertMapper.INSTANCE.AlertToAlertRepositoryEntity(alertToSave.build()))
    );
  }

  public void deleteAlert(String alertId) throws InstanceNotFoundException {

    User user = UserRepositoryEntityMapper.INSTANCE.UserRepositoryEntityToUser(
        userRepository.findById(getActualUserId())
            .orElseThrow(() -> new WrongToken("Token Incorrecto")));

    Alert existingAlert = AlertMapper.INSTANCE.AlertRepositoryEntityToAlert(
        alertRepository.findById(alertId)
            .orElseThrow(() -> new InstanceNotFoundException("Alerta inexistente"))
    );

    if (!existingAlert.owner().equals(user.id())) {
      throw new NotAccesible("Non se poden eliminar alertas axenas");
    }

    alertRepository.deleteById(existingAlert.id());
  }

  public List<Alert> getAlerts() {
    User user = UserRepositoryEntityMapper.INSTANCE.UserRepositoryEntityToUser(
        userRepository.findById(getActualUserId())
            .orElseThrow(() -> new WrongToken("Token Incorrecto"))
    );

    return alertRepository
        .findByOwner(user.id())
        .stream()
        .map(AlertMapper.INSTANCE::AlertRepositoryEntityToAlert)
        .toList();
  }

  public Alert getAlert(String alertId) throws InstanceNotFoundException {
    User user = UserRepositoryEntityMapper.INSTANCE.UserRepositoryEntityToUser(
        userRepository.findById(getActualUserId())
            .orElseThrow(() -> new WrongToken("Token Incorrecto"))
    );

    Alert alert = AlertMapper.INSTANCE.AlertRepositoryEntityToAlert(
        alertRepository.findById(alertId)
            .orElseThrow(() -> new InstanceNotFoundException("Alerta inexistente"))
    );

    if (!alert.owner().equals(user.id())) {
      throw new NotAccesible("Non se poden acceder a alertas axenas");
    }

    return alert;
  }

  public List<Period> checkAlerts() {
    List<Alert> alerts = getAlerts();

    List<Period> periods = new ArrayList<>();
    for (Alert alert : alerts) {
      periods.addAll(weatherChecker.check(alert));
    }

    return periods;
  }

  public List<Period> checkAlert(String alertId) throws InstanceNotFoundException {
    return weatherChecker.check(getAlert(alertId));
  }


}
