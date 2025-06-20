package gal.marevita.anzol.model;

import lombok.Builder;

@Builder(toBuilder = true)
public record Fish(
    String name,
    Integer quantity
) {
}