package com.erikallas.ndl.audit;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuditTestController {

    private final AuditEventRepository repo;

    public AuditTestController(AuditEventRepository repo) {
        this.repo = repo;
    }

    @PostMapping("/api/audit-test")
    public Map<String, Object> writeAuditEvent() {
        UUID id = UUID.randomUUID();
        AuditEvent event = new AuditEvent(
                id,
                null,
                "DEV_TEST",
                "{\"source\":\"api\",\"note\":\"first write\"}",
                OffsetDateTime.now());

        repo.save(event);

        return Map.of("insertedId", id.toString());
    }
}
