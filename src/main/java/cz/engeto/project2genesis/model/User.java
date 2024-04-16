package cz.engeto.project2genesis.model;

import org.springframework.boot.autoconfigure.domain.EntityScan;

import java.util.UUID;


public class User {

    private String name;
    private String surname;
    private String personID;
    private UUID uuid;

    public User(String name, String surname, String personID, UUID uuid) {
        this.name = name;
        this.surname = surname;
        this.personID = personID;
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getPersonID() {
        return personID;
    }

    public void setPersonID(String personID) {
        this.personID = personID;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", personID='" + personID + '\'' +
                ", uuid=" + uuid +
                '}';
    }

}
