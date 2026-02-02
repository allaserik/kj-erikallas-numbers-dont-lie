package com.erikallas.ndl.health.weight;

import com.erikallas.ndl.common.api.dto.PaginatedResponse;
import com.erikallas.ndl.common.api.dto.WeightEntryResponse;
import com.erikallas.ndl.common.api.mapper.ResponseMapper;
import com.erikallas.ndl.common.api.validation.OwnershipValidator;
import com.erikallas.ndl.user.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@SecurityRequirement(name = "bearerAuth")
@RestController
public class WeightController {

    private final UserService userService;
    private final WeightService weightService;
    private final WeightEntryRepository weightRepository;

    public WeightController(UserService userService, WeightService weightService,
            WeightEntryRepository weightRepository) {
        this.userService = userService;
        this.weightService = weightService;
        this.weightRepository = weightRepository;
    }

    public static class AddWeightRequest {
        @DecimalMin(value = "1.0", message = "weightKg must be > 0")
        public double weightKg;

        public OffsetDateTime measuredAt;
        public String note;
    }

    public static class UpdateWeightRequest {
        @DecimalMin(value = "1.0", message = "weightKg must be > 0")
        public Double weightKg;

        public OffsetDateTime measuredAt;
        public String note;
    }

    /**
     * Add a new weight entry.
     * 
     * @param body the weight entry request body
     * @param auth JWT authentication token
     * @return the created weight entry
     */
    @PostMapping("/api/weight")
    @ResponseStatus(HttpStatus.CREATED)
    public WeightEntryResponse add(@Valid @RequestBody AddWeightRequest body, JwtAuthenticationToken auth) {
        var user = userService.ensureUser(auth.getToken().getSubject(), null);
        var entity = weightService.add(user.getId(), body.weightKg, body.measuredAt, body.note);
        return ResponseMapper.toWeightEntryResponse(entity);
    }

    /**
     * Get all weight entries for the authenticated user.
     * 
     * @param auth JWT authentication token
     * @return list of weight entries
     */
    @GetMapping("/api/weight")
    public List<WeightEntryResponse> list(JwtAuthenticationToken auth) {
        var user = userService.ensureUser(auth.getToken().getSubject(), null);
        var entities = weightService.latest(user.getId());
        return entities.stream().map(ResponseMapper::toWeightEntryResponse).toList();
    }

    /**
     * Get a specific weight entry by ID.
     * 
     * @param id   the weight entry ID
     * @param auth JWT authentication token
     * @return the weight entry
     */
    @GetMapping("/api/weight/{id}")
    public WeightEntryResponse get(@PathVariable UUID id, JwtAuthenticationToken auth) {
        var user = userService.ensureUser(auth.getToken().getSubject(), null);
        var entity = weightRepository.findByIdAndUserId(id, user.getId()).orElse(null);
        OwnershipValidator.validateResourceExists(entity != null, "Weight entry");
        return ResponseMapper.toWeightEntryResponse(entity);
    }

    /**
     * Update a weight entry.
     * 
     * @param id   the weight entry ID
     * @param body the update request
     * @param auth JWT authentication token
     * @return the updated weight entry
     */
    @PatchMapping("/api/weight/{id}")
    public WeightEntryResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateWeightRequest body,
            JwtAuthenticationToken auth) {
        var user = userService.ensureUser(auth.getToken().getSubject(), null);
        var entity = weightRepository.findByIdAndUserId(id, user.getId()).orElse(null);
        OwnershipValidator.validateResourceExists(entity != null, "Weight entry");

        if (body.weightKg != null) {
            entity.setWeightKg(body.weightKg);
        }
        if (body.measuredAt != null) {
            entity.setMeasuredAt(body.measuredAt);
        }
        if (body.note != null) {
            entity.setNote(body.note);
        }

        var updated = weightService.update(user.getId(), entity);
        return ResponseMapper.toWeightEntryResponse(updated);
    }

    /**
     * Get paginated weight history for the authenticated user.
     * 
     * @param page the page number (0-indexed)
     * @param size the page size
     * @param auth JWT authentication token
     * @return paginated weight entries
     */
    @GetMapping("/api/weight/history")
    public PaginatedResponse<WeightEntryResponse> getHistory(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size, JwtAuthenticationToken auth) {
        var user = userService.ensureUser(auth.getToken().getSubject(), null);
        var pageRequest = PageRequest.of(page, size, Sort.by("measuredAt").descending());
        var weightPage = weightService.getHistory(user.getId(), pageRequest);

        var content = weightPage.getContent().stream().map(ResponseMapper::toWeightEntryResponse).toList();

        return new PaginatedResponse<>(content, weightPage.getNumber(), weightPage.getSize(),
                weightPage.getTotalElements(), weightPage.getTotalPages(), weightPage.isFirst(), weightPage.isLast());
    }

    /**
     * Delete a weight entry (soft delete).
     * 
     * @param id   the weight entry ID
     * @param auth JWT authentication token
     */
    @DeleteMapping("/api/weight/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id, JwtAuthenticationToken auth) {
        var user = userService.ensureUser(auth.getToken().getSubject(), null);
        var entity = weightRepository.findByIdAndUserId(id, user.getId()).orElse(null);
        OwnershipValidator.validateResourceExists(entity != null, "Weight entry");
        weightRepository.delete(entity);
    }
}
