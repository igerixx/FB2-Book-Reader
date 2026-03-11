package com.Service;

import com.DTO.JwtAuthResponse;
import com.DTO.SignInRequest;
import com.DTO.SignUpRequest;
import com.Entity.CustomUserDetails;
import com.Entity.Role;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    @Getter
    @Setter
    private com.Entity.User currentUser = null;

    public JwtAuthResponse signUp(SignUpRequest sign) {
        if (userService.existsByUsername(sign.getUsername()))
            return new JwtAuthResponse("$User with this username is already exists!");

        var user = User.builder()
                .username(sign.getUsername())
                .password(sign.getPassword())
                .roles("USER")
                .build();

        com.Entity.User us = new com.Entity.User(
                user.getUsername(),
                passwordEncoder.encode(user.getPassword()),
                Role.ROLE_USER
        );

        currentUser = userService.createUser(us);

        return new JwtAuthResponse(jwtService.generateToken(user));
    }

    public JwtAuthResponse signIn(SignInRequest sign) {
        if (!userService.existsByUsername(sign.getUsername()))
            return new JwtAuthResponse("$User with this username does not exists!");

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                sign.getUsername(),
                sign.getPassword()
        ));

        currentUser = userService.findByUsername(sign.getUsername());

        try {
            var user = userDetailsService.loadUserByUsername(sign.getUsername());
            return new JwtAuthResponse(jwtService.generateToken(user));
        } catch (Exception e) {
            return new JwtAuthResponse(null);
        }
    }
}
