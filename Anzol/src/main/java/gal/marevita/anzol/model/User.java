package gal.marevita.anzol.model;

import lombok.Builder;
import lombok.Singular;

import java.util.Set;

@Builder(toBuilder = true)
public record User(
    String id,
    String userName,
    String name,
    String email,
    String password,
    @Singular Set<String> friends,
    @Singular("friendPetitionSent") Set<String> friendPetitionsSent,
    @Singular("friendPetitionReceived") Set<String> friendPetitionsReceived
) {
}

