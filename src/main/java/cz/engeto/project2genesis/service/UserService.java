package cz.engeto.project2genesis.service;

import cz.engeto.project2genesis.model.User;

import java.io.IOException;
import java.util.List;

public interface UserService {
    User createUser(User user) throws IOException;
    User getUserById(String personID, boolean detail);
    List<User> getAllUsers(boolean detail);
    User updateUser(String personID, User user);
    void deleteUserById(String personID);
}
