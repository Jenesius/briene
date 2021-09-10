package com.salat.briene.services;

import com.salat.briene.entities.Article;
import com.salat.briene.entities.User;
import com.salat.briene.entities.Role;
import com.salat.briene.exceptions.*;
import com.salat.briene.payload.request.SignupRequest;
import com.salat.briene.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User loadUserByUsername(String username) throws UserNotFoundException {
        return userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);
    }

    public User loadUserById(Long id) throws UserNotFoundException {
        return userRepository.findById(id).orElseThrow(UserNotFoundException::new);
    }

    public User getUserFromAuthentication(Authentication authentication) throws AnonymousUserException {
        if (authentication == null) {
            throw new AnonymousUserException();
        }
        return loadUserByUsername(authentication.getName());
    }

    public void saveUser(SignupRequest signupRequest)  throws DuplicatedUserException {
        if (existsByUsername(signupRequest.getUsername())) {
            throw new DuplicatedUserByUsernameException();
        }

        if (existsByEmail(signupRequest.getEmail())) {
            throw new DuplicatedUserByEmailException();
        }

        User user = new User();
        user.setUsername(signupRequest.getUsername());
        user.setEmail(signupRequest.getEmail());
        user.setSecretQuestion(signupRequest.getSecretQuestion());
        user.setSecretAnswer(signupRequest.getSecretAnswer());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setRoles(Set.of(new Role(2L, "ROLE_USER")));
        user.setId((long) user.hashCode());
        userRepository.save(user);
    }

    private boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    private boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public void updateUser(Long userId, Map<String, ?> userData) throws UserNotFoundException {
        User userFromDB = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        if (userData.containsKey("email") && userData.get("email") != null) {
            userFromDB.setEmail((String) userData.get("email"));
        }

        if (userData.containsKey("bookmarks") && userData.get("bookmarks") != null) {
            userFromDB.setBookmarkedArticles((Set<Article>) userData.get("bookmarks"));
        }

        if (userData.containsKey("password") && userData.get("password") != null) {
            userFromDB.setPassword(passwordEncoder.encode((String) userData.get("password")));
        }

        userRepository.save(userFromDB);
    }

    private void deleteUser(Long userId) throws UserNotFoundException {
        if (userRepository.findById(userId).isPresent()) {
            userRepository.deleteById(userId);
        } else {
            throw new UserNotFoundException();
        }
    }

    public void changeRole(Long userId, String role) throws UserNotFoundException {
        User userFromDB = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        switch (role) {
            case "blocked" -> userFromDB.setRoles(new HashSet<>(){{
                add(new Role(0L, "ROLE_BLOCKED"));
            }});

            case "admin" -> userFromDB.setRoles(new HashSet<>(){{
                add(new Role(1L, "ROLE_ADMIN"));
            }});

            case "user" -> userFromDB.setRoles(new HashSet<>(){{
                add(new Role(2L, "ROLE_USER"));
            }});
        }
//        mailService.send(userFromDB.getEmail(), "role_change", role);
        userRepository.save(userFromDB);
    }

    public boolean isCurrentPasswordSameAs(Long requiredUserId, String passwordAnother) {
        User requiredUser = this.loadUserById(requiredUserId);
        String requiredUserPassword = requiredUser.getPassword();
        return passwordEncoder.matches(passwordAnother, requiredUserPassword);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}