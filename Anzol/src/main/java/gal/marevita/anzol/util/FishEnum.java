package gal.marevita.anzol.util;

import java.util.Arrays;
import java.util.List;

public enum FishEnum {
  AGULLA,
  AREEIRO,
  CHOCO,
  CONGRO,
  DOURADA,
  ESCACHO,
  FANECA,
  LINGUADO,
  LURA,
  MARAGOTA,
  OLLOMOL,
  PEIXE_SAPO,
  PESCADA,
  PINTO,
  POLBO,
  RODABALLO,
  RAIA,
  ROBALIZA,
  RUDA,
  SANMARTIÑO,
  SARDIÑA,
  SARDA,
  XULIA,
  XURELO;

  public static List<String> getAllFishList() {
    return Arrays.stream(FishEnum.values())
        .map(FishEnum::toString)
        .toList();
  }

  public static FishEnum toFish(String nome) {
    String fish = nome.replace(" ", "_").toUpperCase();
    return FishEnum.valueOf(fish);
  }

  @Override
  public String toString() {
    String nome = name().toLowerCase().replace("_", " ");
    return nome.substring(0, 1).toUpperCase() + nome.substring(1);
  }

}

