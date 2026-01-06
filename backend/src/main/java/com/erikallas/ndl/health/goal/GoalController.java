package com.erikallas.ndl.health.goal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import com.erikallas.ndl.user.UserService;

@RestController
public class GoalController {

    private final UserService userService;
    private final GoalService goalService;

    public GoalController(UserService userService, GoalService goalService) {
        this.userService = userService;
        this.goalService = goalService;
    }

    public static class CreateGoalRequest {
        public GoalType goalType;
        public Double targetWeightKg;

        @Min(0)
        @Max(7)
        public Integer targetActivityDaysPerWeek;

        public String notes;
    }

    public static class UpdateGoalRequest {
        public GoalType goalType;
        public Double targetWeightKg;

        @Min(0)
        @Max(7)
        public Integer targetActivityDaysPerWeek;

        public String notes;
        public Boolean isActive;
    }

    @GetMapping("/api/goals/active")
    public GoalEntity active(JwtAuthenticationToken auth) {
        var user = userService.ensureUser(auth.getToken().getSubject(), null);
        return goalService.getActive(user.getId());
    }

    @PostMapping("/api/goals")
    public GoalEntity create(@Valid @RequestBody CreateGoalRequest body, JwtAuthenticationToken auth) {
        var user = userService.ensureUser(auth.getToken().getSubject(), null);

        if (body.goalType == null) {
            throw new IllegalArgumentException("goalType is required");
        }

        return goalService.createAndActivate(
                user.getId(),
                body.goalType,
                body.targetWeightKg,
                body.targetActivityDaysPerWeek,
                body.notes);
    }

    @PatchMapping("/api/goals/{id}")
    public GoalEntity update(@PathVariable("id") String id, @Valid @RequestBody UpdateGoalRequest body,
            JwtAuthenticationToken auth) {
        var user = userService.ensureUser(auth.getToken().getSubject(), null);
        return goalService.update(
                user.getId(),
                java.util.UUID.fromString(id),
                body.goalType,
                body.targetWeightKg,
                body.targetActivityDaysPerWeek,
                body.notes,
                body.isActive);
    }
}
