package cz.engeto.project2genesis.controller;

import cz.engeto.project2genesis.model.User;
import cz.engeto.project2genesis.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers(@RequestParam(defaultValue = "false") boolean detail) {
        return ResponseEntity.ok(userService.getAllUsers(detail));
    }

    @GetMapping("/user/{uuid}")
    public ResponseEntity<User> getUser(@PathVariable UUID uuid, @RequestParam(defaultValue = "false") boolean detail) {
        try {
            User user = userService.getUserByUUID(uuid, detail);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/user")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User createdUser = userService.createUser(user);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @PutMapping("/user/{uuid}")
    public ResponseEntity<User> updateUser(@PathVariable UUID uuid, @RequestBody User user) {
        try {
            User updatedUser = userService.updateUser(uuid, user);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/user/{uuid}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID uuid) {
        try {
            userService.deleteUserByUUID(uuid);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}