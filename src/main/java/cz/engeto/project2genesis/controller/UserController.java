package cz.engeto.project2genesis.controller;

import cz.engeto.project2genesis.model.User;
import cz.engeto.project2genesis.service.UserService;
import cz.engeto.project2genesis.util.Settings;
import cz.engeto.project2genesis.util.UserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;



@RestController
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    public UserController(UserService userService, Settings settings) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers(@RequestParam(defaultValue = "false") boolean detail) {
        logger.debug("Request to get all users with detail: {}", detail);
        try {
            List<User> users = userService.getAllUsers(detail);
            logger.info("Fetched {} users", users.size());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error fetching all users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean detail) {
        logger.debug("Request to get user by ID: {} with detail: {}", id, detail);
        try {
            User user = userService.getUserById(id, detail);
            logger.info("User fetched with ID: {}", id);
            return ResponseEntity.ok(user);
        } catch (UserException e) {
            logger.error("Error fetching user with ID: {}. {}", id, e.getMessage(), e);
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error fetching user with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    @PostMapping("/user")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        logger.debug("Request to create user: {}", user);
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            logger.warn("Attempt to create user with invalid or empty name");
            return ResponseEntity.badRequest().body("Name is required and cannot be empty.");
        }
        if (user.getPersonID() == null || user.getPersonID().trim().isEmpty()) {
            logger.warn("Attempt to create user with invalid or empty person ID");
            return ResponseEntity.badRequest().body("Person ID is required and cannot be empty.");
        }
        try {
            User createdUser = userService.createUser(user);
            logger.info("User created with ID: {}", createdUser.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (UserException e) {
            logger.error("Error creating user", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            logger.error("IO Exception during user creation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Settings.USER_NOT_CREATED);
        }
    }

    @PutMapping("/user")
    public ResponseEntity<?> updateUser(@RequestBody User user) {
        logger.debug("Request to update user with ID: {}", user.getId());
        if (user.getId() == 0) {
            logger.warn("Attempt to update user without ID");
            return ResponseEntity.badRequest().body("User ID must be provided.");
        }
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            logger.warn("Attempt to update user with invalid or empty name");
            return ResponseEntity.badRequest().body("Name is required and cannot be empty.");
        }
        try {
            User updatedUser = userService.updateUser(user);
            logger.info("User updated with ID: {}", user.getId());
            return ResponseEntity.ok(updatedUser);
        } catch (UserException e) {
            logger.error("Error updating user with ID: {}. {}", user.getId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Settings.USER_NOT_FOUND + user.getId());
        } catch (Exception e) {
            logger.error("Unexpected error updating user with ID: {}", user.getId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Settings.USER_NOT_UPDATED);
        }
    }

    @DeleteMapping("/user/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        logger.debug("Request to delete user with ID: {}", id);
        try {
            userService.deleteUserById(id);
            logger.info("User deleted with ID: {}", id);
            return ResponseEntity.ok(Settings.USER_DELETED);
        } catch (UserException e) {
            logger.error("Error deleting user with ID: {}. {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Settings.USER_NOT_FOUND + id);
        } catch (Exception e) {
            logger.error("Unexpected error deleting user with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete user due to internal server error.");
        }
    }
}