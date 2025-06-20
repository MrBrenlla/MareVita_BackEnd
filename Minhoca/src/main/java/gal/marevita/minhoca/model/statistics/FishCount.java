package gal.marevita.minhoca.model.statistics;


import lombok.Builder;

@Builder(toBuilder = true)
public record FishCount(
    String name,
    int number
) implements Comparable<FishCount> {
  @Override
  public int compareTo(FishCount o) {
    return this.name.compareTo(o.name);
  }
}