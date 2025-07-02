package gal.marevita.commons.repositoryEntities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;


import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "latestCaptures")
public class LatestCapturesRepositoryEntity {

  @Id
  public String id;
  public String owner;
  public int security;
  public GPSLocationRepositoryEntity gpsLocation;
  @Indexed(expireAfterSeconds = 604800) // 7 días en segundos (7*24*60*60)
  public Instant dateTime;
}

