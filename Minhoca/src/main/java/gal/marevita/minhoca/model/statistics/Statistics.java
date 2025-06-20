package gal.marevita.minhoca.model.statistics;

import lombok.Builder;
import lombok.Singular;

import java.util.List;

@Builder(toBuilder = true)
public record Statistics(
    int totalLocations,
    int totalCaptures,
    int totalFishCount,
    CaptureInfo biggerCapture,
    CaptureInfo diverseCapture,
    CaptureInfo likedCapture,
    @Singular List<Location> locations,
    @Singular List<Bait> baits,
    @Singular List<FishCount> fishes
) {

  @Override
  public String toString() {
    return "Statistics{\n" +
        "totalLocations=" + totalLocations + ",\n" +
        "totalCaptures=" + totalCaptures + ",\n" +
        "totalFishCount=" + totalFishCount + ",\n" +
        "biggerCapture=" + (biggerCapture != null ? biggerCapture : "None") + ",\n" +
        "diverseCapture=" + (diverseCapture != null ? diverseCapture : "None") + ",\n" +
        "likedCapture=" + (likedCapture != null ? likedCapture : "None") + ",\n" +
        "locations=" + (locations != null && !locations.isEmpty() ? locations : "None") + ",\n" +
        "baits=" + (baits != null && !baits.isEmpty() ? baits : "None") + ",\n" +
        "fishes=" + (fishes != null && !fishes.isEmpty() ? fishes : "None") + "\n" +
        '}';
  }
}