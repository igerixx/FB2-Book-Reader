package com.Controller;

import com.DTO.JwtAuthResponse;
import com.DTO.SignInRequest;
import com.DTO.SignUpRequest;
import com.Service.AuthService;
import com.Service.BookService;
import com.Service.UserService;
import com.igerixx.Reader.XMLAttribute;
import com.igerixx.Reader.XMLReader;
import com.igerixx.Reader.XMLReaderConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MainController {
    private final BookService bookService;
    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/signin")
    public JwtAuthResponse signIn(@RequestBody SignInRequest sign) {
        return authService.signIn(sign);
    }

    @PostMapping("/signup")
    public JwtAuthResponse signUp(@RequestBody SignUpRequest sign) {
        return authService.signUp(sign);
    }

    @PostMapping("/logout")
    public void logOut(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        SecurityContextHolder.clearContext();
        authService.setCurrentUser(null);
    }

    @GetMapping("/book")
    public Map<String, String> map() {
        return Map.of("status", "ok");
    }

    @PostMapping("/uploadByF")
    public ResponseEntity<Map<String, ?>> upload(@RequestParam("data") MultipartFile multipartFile) throws IOException {
        return bookService.upload(multipartFile, null);
    }

    @PostMapping("/uploadByH")
    public ResponseEntity<Map<String, ?>> upload(@RequestBody String filename) throws IOException {
        return bookService.upload(null, filename);
    }

    @PostMapping("books")
    public ResponseEntity<Map<String, ?>> books(@RequestBody String username) {
        return bookService.books(username);
    }

    @PostMapping("/currentUser")
    public ResponseEntity<Map<String, String>> currentUser() {
        return ResponseEntity
            .ok()
            .body(Map.of(
                    "user", authService.getCurrentUser() != null
                            ? authService.getCurrentUser().getUsername()
                            : "$none"
            ));
    }
}
