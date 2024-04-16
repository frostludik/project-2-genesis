package cz.engeto.project2genesis.service;

import cz.engeto.project2genesis.model.User;
import cz.engeto.project2genesis.util.Settings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    private final JdbcTemplate jdbcTemplate;
    private final Settings settings;

    @Autowired
    public UserServiceImpl(JdbcTemplate jdbcTemplate, Settings settings) {
        this.jdbcTemplate = jdbcTemplate;
        this.settings = settings;
    }

    private String getNextPersonID() throws IOException {
        Path path = settings.getPersonIdFilePath();
        List<String> personIds = Files.readAllLines(path);
        if (!personIds.isEmpty()) {
            String personID = personIds.get(0);
            Files.write(path, personIds.subList(1, personIds.size()));
            return personID;
        } else {
            throw new IllegalStateException("No available Person IDs.");
        }
    }

    private RowMapper<User> rowMapper = (rs, rowNum) -> new User(
            rs.getString("name"),
            rs.getString("surname"),
            rs.getString("personID"),
            UUID.fromString(rs.getString("uuid"))
    );

    @Override
    public User createUser(User user) throws IOException {
        String personID = getNextPersonID(); // Ensure uniqueness from file
        UUID uuid = UUID.randomUUID(); // Automatically generate UUID

        user.setPersonID(personID);
        user.setUuid(uuid);

        String sql = "INSERT INTO users (name, surname, personID, uuid) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, user.getName(), user.getSurname(), personID, uuid.toString());
        return user;
    }

    @Override
    public User getUserById(String personID, boolean detail) {
        String sql = detail
                ? "SELECT * FROM users WHERE personID = ?"
                : "SELECT name, surname FROM users WHERE personID = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{personID}, rowMapper);
    }

    @Override
    public List<User> getAllUsers(boolean detail) {
        String sql = detail
                ? "SELECT * FROM users"
                : "SELECT name, surname, personID FROM users"; // Include personID in the non-detailed view
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public User updateUser(String personID, User user) {
        String sql = "UPDATE users SET name = ?, surname = ? WHERE personID = ?";
        jdbcTemplate.update(sql, user.getName(), user.getSurname(), personID);
        return user;
    }

    @Override
    public void deleteUserById(String personID) {
        String sql = "DELETE FROM users WHERE personID = ?";
        jdbcTemplate.update(sql, personID);
    }
}
