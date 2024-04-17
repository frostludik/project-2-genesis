package cz.engeto.project2genesis.service;

import cz.engeto.project2genesis.model.User;
import cz.engeto.project2genesis.util.Settings;
import cz.engeto.project2genesis.util.UserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.PreparedStatement;
import java.sql.Statement;
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

    private boolean isValidPersonID(String personID) throws IOException {
        Resource resource = settings.getPersonIdResource();
        try (InputStream inputStream = resource.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            List<String> personIds = reader.lines().toList();
            return personIds.contains(personID);
        } catch (IOException e) {
            throw new UserException("Failed to access person ID resource.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private RowMapper<User> rowMapper = (rs, rowNum) -> new User(
            rs.getLong("ID"),
            rs.getString("name"),
            rs.getString("surname"),
            rs.getString("personID"),
            UUID.fromString(rs.getString("uuid"))
    );

    private RowMapper<User> simpleRowMapper = (rs, rowNum) -> new User(
            rs.getLong("ID"),
            rs.getString("name"),
            rs.getString("surname")
    );


    private boolean personIDExists(String personID) {
        String sql = "SELECT COUNT(*) FROM users WHERE personID = ?";
        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{personID}, Integer.class);
        return count != null && count > 0;
    }

    private boolean idExists(Long id) {
        String sql = "SELECT COUNT(*) FROM users WHERE ID = ?";
        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{id}, Integer.class);
        return count != null && count > 0;
    }

    @Override
    public User createUser(User user) throws IOException {
        if (!isValidPersonID(user.getPersonID())) {
            throw new UserException("Invalid or unavailable Person ID.", HttpStatus.BAD_REQUEST);
        }

        if (personIDExists(user.getPersonID())) {
            throw new UserException("Person ID is already assigned to a different user.", HttpStatus.CONFLICT);
        }

        UUID uuid = UUID.randomUUID();
        user.setUuid(uuid);

        String sql = "INSERT INTO users (name, surname, personID, uuid) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getName());
            ps.setString(2, user.getSurname());
            ps.setString(3, user.getPersonID());
            ps.setString(4, uuid.toString());
            return ps;
        }, keyHolder);
        user.setId(keyHolder.getKey().longValue());
        return user;
    }

    @Override
    public User getUserById(Long id, boolean detail) {
        try {
            String sql = detail
                    ? "SELECT * FROM users WHERE id = ?"
                    : "SELECT id, name, surname FROM users WHERE id = ?";
            RowMapper<User> selectedMapper = detail ? rowMapper : simpleRowMapper;
            return jdbcTemplate.queryForObject(sql, new Object[]{id}, selectedMapper);
        } catch (EmptyResultDataAccessException ex) {
            throw new UserException("User not found with id: " + id, HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public List<User> getAllUsers(boolean detail) {
        String sql = detail
                ? "SELECT * FROM users"
                : "SELECT id, name, surname, personID, uuid FROM users";
        RowMapper<User> selectedMapper = detail ? rowMapper : simpleRowMapper;
        return jdbcTemplate.query(sql, selectedMapper);
    }

    @Override
    public User updateUser(User user) {
        Long id = user.getId();
        if (!idExists(id)) {
            throw new UserException("User not found with id: " + id, HttpStatus.NOT_FOUND);
        }

        String sql = "UPDATE users SET name = ?, surname = ? WHERE id = ?";
        jdbcTemplate.update(sql, user.getName(), user.getSurname(), id);
        return user;
    }

    @Override
    public void deleteUserById(Long id) {
        if (!idExists(id)) {
            throw new UserException("User not found with id: " + id, HttpStatus.NOT_FOUND);
        }

        String sql = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
