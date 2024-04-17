package cz.engeto.project2genesis.service;

import cz.engeto.project2genesis.model.User;
import cz.engeto.project2genesis.util.Settings;
import cz.engeto.project2genesis.util.UserException;
import cz.engeto.project2genesis.util.ValidationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    public UserServiceImpl(JdbcTemplate jdbcTemplate, Settings settings) {
        this.jdbcTemplate = jdbcTemplate;
        this.settings = settings;
    }

    private boolean isValidPersonID(String personID) throws IOException {
        logger.debug("Checking validity of Person ID: {}", personID);
        try {
            ValidationUtils.validatePersonID(personID);
            logger.debug("Person ID {} passed initial validation checks.", personID);
        } catch (IllegalArgumentException e) {
            logger.error("Validation error for Person ID {}: {}", personID, e.getMessage());
            throw e;
        }

        Resource resource = settings.getPersonIdResource();
        try (InputStream inputStream = resource.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            List<String> personIds = reader.lines().toList();
            boolean isValid = personIds.contains(personID);
            logger.debug("Person ID {} is valid: {}", personID, isValid);
            return isValid;
        } catch (IOException e) {
            logger.error("Failed to access person ID resource", e);
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
        logger.debug("Checking if Person ID exists in DB: {}", personID);
        String sql = "SELECT COUNT(*) FROM users WHERE personID = ?";
        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{personID}, Integer.class);
        boolean exists = count != null && count > 0;
        logger.debug("Person ID {} exists in DB: {}", personID, exists);
        return exists;
    }

    private boolean idExists(Long id) {
        logger.debug("Checking if User ID exists: {}", id);
        String sql = "SELECT COUNT(*) FROM users WHERE ID = ?";
        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{id}, Integer.class);
        boolean exists = count != null && count > 0;
        logger.debug("User ID {} exists: {}", id, exists);
        return exists;
    }

    @Override
    public User createUser(User user) throws IOException {
        logger.info("Creating user: {}", user);
        try {
            ValidationUtils.validatePersonID(user.getPersonID());
        } catch (IllegalArgumentException e) {
            logger.error("Validation error for personID: {}", user.getPersonID(), e);
            throw new UserException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        if (!isValidPersonID(user.getPersonID())) {
            logger.warn("Invalid or unavailable Person ID for user: {}", user);
            throw new UserException("Invalid or unavailable Person ID.", HttpStatus.BAD_REQUEST);
        }

        if (personIDExists(user.getPersonID())) {
            logger.warn("Attempt to assign already existing Person ID to a different user: {}", user);
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
        logger.info("User created with ID: {}", user.getId());
        return user;
    }

    @Override
    public User getUserById(Long id, boolean detail) {
        logger.info("Retrieving user by ID: {}", id);
        try {
            String sql = detail
                    ? "SELECT * FROM users WHERE id = ?"
                    : "SELECT id, name, surname FROM users WHERE id = ?";
            RowMapper<User> selectedMapper = detail ? rowMapper : simpleRowMapper;
            return jdbcTemplate.queryForObject(sql, new Object[]{id}, selectedMapper);
        } catch (EmptyResultDataAccessException ex) {
            logger.error("User with ID {} not found", id, ex);
            throw new UserException("User with id: " + id + " not found!", HttpStatus.NOT_FOUND);
        }
    }


    @Override
    public List<User> getAllUsers(boolean detail) {
        logger.info("Retrieving all users, detailed: {}", detail);
        String sql = detail
                ? "SELECT * FROM users"
                : "SELECT id, name, surname, personID, uuid FROM users";
        RowMapper<User> selectedMapper = detail ? rowMapper : simpleRowMapper;
        return jdbcTemplate.query(sql, selectedMapper);
    }

    @Override
    public User updateUser(User user) {
        logger.info("Updating user: {}", user);
        Long id = user.getId();
        if (!idExists(id)) {
            logger.warn("Attempt to update non-existing user ID: {}", id);
            throw new UserException("User with id: " + id + " not found!", HttpStatus.NOT_FOUND);
        }

        String sql = "UPDATE users SET name = ?, surname = ? WHERE id = ?";
        jdbcTemplate.update(sql, user.getName(), user.getSurname(), id);
        logger.info("User updated: {}", user);
        return user;
    }

    @Override
    public void deleteUserById(Long id) {
        logger.info("Deleting user ID: {}", id);
        if (!idExists(id)) {
            logger.error("Attempt to delete non-existing user ID: {}", id);
            throw new UserException("User with id: " + id + " not found!", HttpStatus.NOT_FOUND);
        }

        String sql = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(sql, id);
        logger.info("User deleted: {}", id);
    }
}
