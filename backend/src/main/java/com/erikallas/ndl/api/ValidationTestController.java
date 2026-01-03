package com.erikallas.ndl.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ValidationTestController {

  public record DemoRequest(
      @NotBlank(message = "name is required") String name,
      @Email(message = "email must be valid") String email
  ) {}

  @PostMapping("/api/validate-test")
  public Map<String, Object> validate(@Valid @RequestBody DemoRequest body) {
    return Map.of("ok", true, "name", body.name(), "email", body.email());
  }
}
