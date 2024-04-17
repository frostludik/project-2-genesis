package cz.engeto.project2genesis.service;

import cz.engeto.project2genesis.model.User;

import java.io.IOException;
import java.util.List;

public interface UserService {
    User createUser(User user) throws IOException;
    User getUserById(Long id, boolean detail);
    List<User> getAllUsers(boolean detail);
    User updateUser(User user);
    void deleteUserById(Long id);
}
