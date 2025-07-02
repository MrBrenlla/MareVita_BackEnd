package gal.marevita.commons.repositoryEntities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "alerts")
public class AlertRepositoryEntity {

  @Id
  public String id;
  public String name;
  @Indexed
  public String owner;
  public String relatedCapture;
  public GPSLocationRepositoryEntity gpsLocation;
  @Indexed
  public String location;
  public List<String> baits = new ArrayList<>();
  public List<FishRepositoryEntity> fish = new ArrayList<>();
  public List<WeatherConditionRepositoryEntity> weatherConditions = new ArrayList<>();

}
