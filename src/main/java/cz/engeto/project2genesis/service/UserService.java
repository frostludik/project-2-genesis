package cz.engeto.project2genesis.service;

import cz.engeto.project2genesis.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private RowMapper<User> rowMapper = (rs, rowNum) -> new User(
            rs.getString("name"),
            rs.getString("surname"),
            rs.getString("personID"),
            UUID.fromString(rs.getString("uuid"))
    );

    public User createUser(User user) {
        // Automatically generate the UUID
        user.setUuid(UUID.randomUUID());

        String sql = "INSERT INTO users (name, surname, personID, uuid) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, user.getName(), user.getSurname(), user.getPersonID(), user.getUuid().toString());
        return user;
    }

    public User getUserByUUID(UUID uuid, boolean detail) {
        String sql = detail
                ? "SELECT * FROM users WHERE uuid = ?"
                : "SELECT name, surname FROM users WHERE uuid = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{uuid.toString()}, rowMapper);
    }

    public List<User> getAllUsers(boolean detail) {
        String sql = detail
                ? "SELECT * FROM users"
                : "SELECT name, surname FROM users";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public User updateUser(UUID uuid, User user) {
        String sql = "UPDATE users SET name = ?, surname = ?, personID = ? WHERE uuid = ?";
        jdbcTemplate.update(sql, user.getName(), user.getSurname(), user.getPersonID(), uuid.toString());
        return user;
    }

    public void deleteUserByUUID(UUID uuid) {
        String sql = "DELETE FROM users WHERE uuid = ?";
        jdbcTemplate.update(sql, uuid.toString());
    }
}




