package com.erikallas.ndl.health.weight;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import com.erikallas.ndl.user.service.UserService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@SecurityRequirement(name = "bearerAuth")
@RestController
public class WeightController {

    private final UserService userService;
    private final WeightService weightService;

    public WeightController(UserService userService, WeightService weightService) {
        this.userService = userService;
        this.weightService = weightService;
    }

    public static class AddWeightRequest {
        @DecimalMin(value = "1.0", message = "weightKg must be > 0")
        public double weightKg;

        public OffsetDateTime measuredAt;
        public String note;
    }

    @PostMapping("/api/weight")
    public WeightEntryEntity add(@Valid @RequestBody AddWeightRequest body, JwtAuthenticationToken auth) {
        var user = userService.ensureUser(auth.getToken().getSubject(), null);
        return weightService.add(user.getId(), body.weightKg, body.measuredAt, body.note);
    }

    @GetMapping("/api/weight")
    public List<WeightEntryEntity> list(JwtAuthenticationToken auth) {
        var user = userService.ensureUser(auth.getToken().getSubject(), null);
        return weightService.latest(user.getId());
    }
}
