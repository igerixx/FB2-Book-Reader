package com.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class EPController {
    @GetMapping("/")
    public String home() {
        return "forward:/index.html";
    }

    @GetMapping("/login")
    public String login() {
        return "forward:/logWindow.html";
    }

    @GetMapping("/history")
    public String history() {
        return "forward:/historyWindow.html";
    }

    @GetMapping("/account")
    public String account() {
        return "forward:/accountWindow.html";
    }
}
