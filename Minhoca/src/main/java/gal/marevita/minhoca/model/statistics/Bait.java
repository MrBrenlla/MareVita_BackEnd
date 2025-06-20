package gal.marevita.minhoca.model.statistics;


import lombok.Builder;
import lombok.Singular;

import java.util.List;

@Builder(toBuilder = true)
public record Bait(
    String name,
    int times,
    @Singular List<FishCount> fishes
) implements Comparable<Bait> {
  @Override
  public int compareTo(Bait o) {
    return this.name.compareTo(o.name);
  }
}