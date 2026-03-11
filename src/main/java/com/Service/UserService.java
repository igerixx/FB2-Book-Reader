package com.Service;

import com.Entity.Book;
import com.Entity.User;
import com.Repository.BookRepository;
import com.Repository.UserRepository;
import com.Security.SecurityConfig;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private User user;
    private Book book;
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDetailsService userDetailsService() {
        return new CustomUserDetailsService();
    }

    public User getCurrentUser() {
        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        return findByUsername(username);
    }

    public void deleteUser(User user) {
        userRepository.delete(user);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public User createUser(User user) {
        if (existsByUsername(user.getUsername())) {
            return null;
        }
        return saveUser(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow();
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
}
