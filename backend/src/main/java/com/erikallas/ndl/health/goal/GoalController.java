package com.erikallas.ndl.health.goal;

import com.erikallas.ndl.auth.user.UserService;
import com.erikallas.ndl.common.api.dto.GoalResponse;
import com.erikallas.ndl.common.api.mapper.ResponseMapper;
import com.erikallas.ndl.common.api.validation.OwnershipValidator;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@SecurityRequirement(name = "bearerAuth")
@RestController
public class GoalController {

    private final UserService userService;
    private final GoalService goalService;
    private final GoalRepository goalRepository;

    public GoalController(UserService userService, GoalService goalService, GoalRepository goalRepository) {
        this.userService = userService;
        this.goalService = goalService;
        this.goalRepository = goalRepository;
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

    /**
     * Get the active goal for the authenticated user.
     * 
     * @param auth JWT authentication token
     * @return the active goal
     */
    @GetMapping("/api/goals/active")
    public GoalResponse active(JwtAuthenticationToken auth) {
        var user = userService.ensureUser(auth.getToken().getSubject(), null);
        var entity = goalService.getActive(user.getId());
        return ResponseMapper.toGoalResponse(entity);
    }

    /**
     * Get all goals (active and inactive) for the authenticated user.
     * 
     * @param auth JWT authentication token
     * @return list of all goals for the user, ordered by creation date descending
     */
    @GetMapping("/api/goals")
    public List<GoalResponse> getAll(JwtAuthenticationToken auth) {
        var user = userService.ensureUser(auth.getToken().getSubject(), null);
        var entities = goalRepository.findAllByUserIdNotDeleted(user.getId());
        return entities.stream().map(ResponseMapper::toGoalResponse).collect(Collectors.toList());
    }

    /**
     * Create a new goal for the authenticated user. Only one active goal allowed
     * per user.
     * 
     * @param body the goal creation request
     * @param auth JWT authentication token
     * @return the created goal
     */
    @PostMapping("/api/goals")
    @ResponseStatus(HttpStatus.CREATED)
    public GoalResponse create(@Valid @RequestBody CreateGoalRequest body, JwtAuthenticationToken auth) {
        var user = userService.ensureUser(auth.getToken().getSubject(), null);

        if (body.goalType == null) {
            throw new IllegalArgumentException("goalType is required");
        }

        var entity = goalService.createAndActivate(user.getId(), body.goalType, body.targetWeightKg,
                body.targetActivityDaysPerWeek, body.notes);
        return ResponseMapper.toGoalResponse(entity);
    }

    /**
     * Get a specific goal by ID.
     * 
     * @param id   the goal ID
     * @param auth JWT authentication token
     * @return the goal
     */
    @GetMapping("/api/goals/{id}")
    public GoalResponse get(@PathVariable UUID id, JwtAuthenticationToken auth) {
        var user = userService.ensureUser(auth.getToken().getSubject(), null);
        var entity = goalRepository.findByIdAndUserId(id, user.getId()).orElse(null);
        OwnershipValidator.validateResourceExists(entity != null, "Goal");
        return ResponseMapper.toGoalResponse(entity);
    }

    /**
     * Update a goal.
     * 
     * @param id   the goal ID
     * @param body the goal update request
     * @param auth JWT authentication token
     * @return the updated goal
     */
    @PatchMapping("/api/goals/{id}")
    public GoalResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateGoalRequest body,
            JwtAuthenticationToken auth) {
        var user = userService.ensureUser(auth.getToken().getSubject(), null);
        var entity = goalRepository.findByIdAndUserId(id, user.getId()).orElse(null);
        OwnershipValidator.validateResourceExists(entity != null, "Goal");

        var updated = goalService.update(user.getId(), id, body.goalType, body.targetWeightKg,
                body.targetActivityDaysPerWeek, body.notes, body.isActive);
        return ResponseMapper.toGoalResponse(updated);
    }

    /**
     * Delete a goal (soft delete).
     * 
     * @param id   the goal ID
     * @param auth JWT authentication token
     */
    @DeleteMapping("/api/goals/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id, JwtAuthenticationToken auth) {
        var user = userService.ensureUser(auth.getToken().getSubject(), null);
        var entity = goalRepository.findByIdAndUserId(id, user.getId()).orElse(null);
        OwnershipValidator.validateResourceExists(entity != null, "Goal");
        goalRepository.delete(entity);
    }
}
