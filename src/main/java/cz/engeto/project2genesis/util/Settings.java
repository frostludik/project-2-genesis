package cz.engeto.project2genesis.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;


@Component
public class Settings {
    @Autowired
    private ResourceLoader resourceLoader;

    public Resource getPersonIdResource() {
        return resourceLoader.getResource("classpath:dataPersonId.txt");
    }
}
