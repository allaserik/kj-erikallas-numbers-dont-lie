# Encryption At Rest Plan

Status: app-layer baseline implemented; production infra hardening still planned

## Current State

- Database stores health/profile/auth data in PostgreSQL.
- Transport security is expected via HTTPS/TLS in deployment.
- Application now protects sensitive auth secrets at rest:
  - refresh tokens, password reset tokens, verification codes are stored as hashes
  - 2FA secrets are encrypted with AES-GCM
- Remaining gap is infra-level evidence and rollout (encrypted volumes/KMS policy enforcement).

## Practical Production Plan

1. Disk/volume encryption (baseline)
- Use encrypted block storage for DB volumes (cloud-managed encryption by default where available).
- Ensure snapshot backups are encrypted.

2. Managed key management
- Use KMS-managed keys (AWS KMS / GCP KMS / Azure Key Vault equivalent).
- Separate key admin from app admin access.
- Rotate keys periodically and on incident.

3. Database-level encryption controls
- Enforce encrypted connections to DB from application.
- For sensitive columns (tokens/secrets), use application-level encryption before write.

4. Application-level field encryption (target fields)
- Priority fields:
  - password reset tokens
  - refresh tokens
  - 2FA secrets
  - optional: user email (if stronger privacy requirement)
- Use authenticated encryption (AES-GCM) with key material from KMS-backed secret source.
- Store key version with ciphertext to support rotation.

5. Operational controls
- Restrict DB access by role (least privilege).
- Audit access to backup storage and key usage.
- Verify encryption settings in infra CI checks.

## Why This Meets Requirement Intent

- Health-data risk is reduced even if storage media is exposed.
- Secrets are protected independently of raw DB file access.
- Key lifecycle and separation of duties are explicitly addressed.

## Scope Decision For This Submission

- App-layer protections for sensitive auth data are implemented now.
- Infra-level at-rest controls are documented as next production phase.
- Reviewer can validate current implementation plus concrete production rollout path.
