package cz.engeto.project2genesis.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class Settings {
    @Value("${person.id.file.path:dataPersonId.txt}")
    private String personIdFilePath;

    public Path getPersonIdFilePath() {
        return Paths.get(personIdFilePath);
    }
}
