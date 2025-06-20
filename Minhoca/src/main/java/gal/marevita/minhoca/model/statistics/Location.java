package gal.marevita.minhoca.model.statistics;


import lombok.Builder;
import lombok.Singular;

import java.util.List;

@Builder(toBuilder = true)
public record Location(
    String name,
    int times,
    @Singular List<FishCount> fishes
) implements Comparable<Location> {
  @Override
  public int compareTo(Location o) {
    return this.name.compareTo(o.name);
  }
}