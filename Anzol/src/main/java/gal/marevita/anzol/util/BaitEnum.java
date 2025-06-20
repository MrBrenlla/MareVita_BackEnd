package gal.marevita.anzol.util;

import java.util.Arrays;
import java.util.List;

public enum BaitEnum {
  CAMARÓN,
  CANGREXO,
  CULLER,
  GAMBA,
  LAGOSTINO,
  LUBIÓN,
  LURA,
  MEXILLÓN,
  MIÑOCA_AMERICANA,
  MIÑOCA_COREANA,
  NAVALLA,
  PASEANTE,
  PLUMA,
  POLBO,
  POTERA,
  RAPALA,
  SARDIÑA,
  TITA,
  VINILO,
  XARDA;

  public static List<String> getAllBaitList() {
    return Arrays.stream(BaitEnum.values())
        .map(BaitEnum::toString)
        .toList();
  }

  @Override
  public String toString() {
    String nome = name().toLowerCase().replace("_", " ");
    return nome.substring(0, 1).toUpperCase() + nome.substring(1);
  }
}

