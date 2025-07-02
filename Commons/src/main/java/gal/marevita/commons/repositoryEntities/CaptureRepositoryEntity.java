package gal.marevita.commons.repositoryEntities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "captures")
public class CaptureRepositoryEntity {

  @Id
  public String id;
  @Indexed
  public String owner;
  public int security;
  public List<String> likes = new ArrayList<>();
  public String dateTime;
  public GPSLocationRepositoryEntity gpsLocation;
  @Indexed
  public String location;
  public String imageCaption;
  public List<String> images = new ArrayList<>();
  public List<String> baits = new ArrayList<>();
  public List<FishRepositoryEntity> fish = new ArrayList<>();
  public List<WeatherConditionRepositoryEntity> weatherConditions = new ArrayList<>();

  // Getters e setters
  // Constructor sen argumentos e outros que precises
}

