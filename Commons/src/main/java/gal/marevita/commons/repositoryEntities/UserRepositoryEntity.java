package gal.marevita.commons.repositoryEntities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.Set;

@Document(collection = "users")
public class UserRepositoryEntity {
    @Id
    public String id;
    @Indexed
    public String userName;
    public String name;
    @Indexed
    public String email;
    public String password;
    public Set<String> friends = new HashSet<>();
    public Set<String> friendPetitionsSent = new HashSet<>();
    public Set<String> friendPetitionsReceived = new HashSet<>();

}
