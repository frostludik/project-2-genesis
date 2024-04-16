package cz.engeto.project2genesis.service;

import cz.engeto.project2genesis.model.User;
import cz.engeto.project2genesis.util.Settings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final JdbcTemplate jdbcTemplate;
    private Settings settings;

    @Autowired
    public UserServiceImpl(JdbcTemplate jdbcTemplate, Settings settings) {
        this.jdbcTemplate = jdbcTemplate;
        this.settings = settings;
    }

    private boolean isValidPersonID(String personID) throws IOException {
        Resource resource = settings.getPersonIdResource();
        try (InputStream inputStream = resource.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            List<String> personIds = reader.lines().toList();
            return personIds.contains(personID);
        } catch (IOException e) {
            throw new IOException("Failed to access person ID resource", e);
        }
    }

    private RowMapper<User> rowMapper = (rs, rowNum) -> new User(
            rs.getString("name"),
            rs.getString("surname"),
            rs.getString("personID"),
            UUID.fromString(rs.getString("uuid"))
    );

    private boolean personIDExists(String personID) {
        String sql = "SELECT COUNT(*) FROM users WHERE personID = ?";
        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{personID}, Integer.class);
        return count != null && count > 0;
    }

    @Override
    public User createUser(User user) throws IOException {
        // Check if the Person ID is valid and not used already
        if (!isValidPersonID(user.getPersonID())) {
            throw new IllegalStateException("Invalid or unavailable Person ID.");
        }

        if (personIDExists(user.getPersonID())) {
            throw new IllegalStateException("Person ID is already assigned to a different user.");
        }

        UUID uuid = UUID.randomUUID();
        user.setUuid(uuid);

        String sql = "INSERT INTO users (name, surname, personID, uuid) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, user.getName(), user.getSurname(), user.getPersonID(), uuid.toString());
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
                : "SELECT name, surname, personID FROM users";
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
