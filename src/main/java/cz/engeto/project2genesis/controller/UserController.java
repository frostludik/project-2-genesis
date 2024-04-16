package cz.engeto.project2genesis.controller;

import cz.engeto.project2genesis.model.User;
import cz.engeto.project2genesis.service.UserService;
import cz.engeto.project2genesis.util.Settings;
import cz.engeto.project2genesis.util.UserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;


@RestController
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;
    private final Settings settings;

    @Autowired
    public UserController(UserService userService, Settings settings) {
        this.userService = userService;
        this.settings = settings;
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers(@RequestParam(defaultValue = "false") boolean detail) {
        List<User> users = userService.getAllUsers(detail);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean detail) {
        try {
            User user = userService.getUserById(id, detail);
            return ResponseEntity.ok(user);
        } catch (UserException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    @PostMapping("/user")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            User createdUser = userService.createUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (UserException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(settings.USER_NOT_CREATED);
        }
    }

    @PutMapping("/user/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User user) {
        try {
            User updatedUser = userService.updateUser(id, user);
            return ResponseEntity.ok(updatedUser);
        } catch (UserException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(settings.USER_NOT_FOUND + id);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(settings.USER_NOT_UPDATED);
        }
    }

    @DeleteMapping("/user/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUserById(id);
            return ResponseEntity.ok(settings.USER_DELETED);
        } catch (UserException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(settings.USER_NOT_FOUND + id);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete user due to internal server error.");
        }
    }
}