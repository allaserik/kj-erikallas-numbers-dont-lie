package com.erikallas.ndl.api;

// GET api/ping returns{"status":"ok"}
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PingController {

    @GetMapping("/ping")
    public String ping() {
        return "{ \"status\": \"ok\" }";
    }
}