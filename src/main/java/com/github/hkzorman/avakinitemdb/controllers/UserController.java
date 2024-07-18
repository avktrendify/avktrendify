package com.github.hkzorman.avakinitemdb.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.hkzorman.avakinitemdb.models.db.User;
import com.github.hkzorman.avakinitemdb.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Base64;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/users")
public class UserController {

    private Logger logger = LoggerFactory.getLogger(UserController.class);

    private AuthenticationManager authenticationManager;
    private InMemoryUserDetailsManager userDetailsService;
    private UserRepository repository;

    @Autowired
    public UserController(AuthenticationManager authenticationManager, InMemoryUserDetailsManager userDetailsService, UserRepository repository) {
        this.repository = repository;
        this.userDetailsService = userDetailsService;
        this.authenticationManager = authenticationManager;
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @GetMapping("/getUser")
    public ResponseEntity<AuthenticationResponse> getUserData() throws Exception {

        var principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal == null) {
            return ResponseEntity.badRequest().body(null);
        }

        var username = ((org.springframework.security.core.userdetails.User)principal).getUsername();
        var userDetails = this.userDetailsService.loadUserByUsername(username);

        var password = userDetails.getPassword();
        var authorities = userDetails.getAuthorities();

        var canCreate = authorities.stream().anyMatch(x -> x.getAuthority().toLowerCase().contains("admin"));
        var auth = Base64
                .getEncoder()
                .encode((username + ":" + password).getBytes());

        return ResponseEntity.ok(new AuthenticationResponse(canCreate, new String(auth)));
    }

    @PostMapping("/save")
    public ResponseEntity<String> save(@RequestBody User user) throws JsonProcessingException {
        var mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        if (user.getId() == null || user.getId().isEmpty()) {
            // Validate password if trying to register an admin
            if (user.getAdmin() && user.getPassword().length() < 6) {
                return ResponseEntity
                        .badRequest()
                        .body("La contraseña debe ser mínimo de 6 caracteres o más.");
            }
            else if (!user.getAdmin()) {
                user.setPassword("dummy");
            }

            var existingUser = repository.findByUsername(user.getUsername());
            if (existingUser.isPresent())
                return ResponseEntity.badRequest().body("El usuario con nombre " + user.getUsername() + " ya está registrado. Por favor utilice otro nombre.");

            user.setCreatedOn(LocalDateTime.now());
            user.setActive(true);

            // Add user
            var details = org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
                    .password(user.getPassword())
                    .build();
            userDetailsService.createUser(details);

            logger.info("User with name '" + user.getUsername() + "' created. Authenticating...");

            // Authenticate user
            var authentication = new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword());
            SecurityContextHolder.getContext().setAuthentication(authentication);


            return ResponseEntity.ok(mapper.writeValueAsString(repository.save(user)));
        }
        else {
            var existingUserOpt = repository.findById(user.getId());
            if (existingUserOpt.isPresent()) {
                var existingUser = existingUserOpt.get();
                existingUser.setActive(user.getActive());
                return ResponseEntity.ok(mapper.writeValueAsString(existingUser));
            }
        }

        return null;
    }

    public static class AuthenticationResponse {
        private boolean canCreate;
        private String auth;

        public AuthenticationResponse(boolean canCreate, String auth) {
            this.canCreate = canCreate;
            this.auth = auth;
        }

        public boolean isCanCreate() {
            return canCreate;
        }

        public void setCanCreate(boolean canCreate) {
            this.canCreate = canCreate;
        }

        public String getAuth() {
            return auth;
        }

        public void setAuth(String auth) {
            this.auth = auth;
        }
    }

}
