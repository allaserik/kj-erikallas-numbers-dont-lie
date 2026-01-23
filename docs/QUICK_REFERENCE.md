# Quick Reference: Architecture & Decisions

## User Journey at a Glance

```
REGISTER
    ↓
VERIFY EMAIL (soft gate - shows alert but app usable)
    ↓
GDPR CONSENT (hard gate - must agree)
    ↓
SETUP PROFILE (optional - 5 steps, can skip each)
    ↓
DASHBOARD (main app, shows progress)
    ↓
OPTIONAL: 2FA SETUP
```

## Database Tables

| Table                      | Purpose                | Key Fields                               |
| -------------------------- | ---------------------- | ---------------------------------------- |
| `users`                    | User accounts          | id, email, password_hash, email_verified |
| `email_verification_codes` | Verify email ownership | code, expires_at, verified_at            |
| `password_reset_tokens`    | Password reset         | token, expires_at, used_at               |
| `consent_records`          | GDPR tracking          | consent_type, agreed, agreed_at          |
| `two_factor_settings`      | 2FA config             | method, secret_encrypted, is_enabled     |
| `health_profiles`          | Health data            | All 20+ fields from comprehensive form   |
| `weight_entries`           | Weight tracking        | weight_kg, measured_at                   |

## API Endpoints (Summary)

### Auth

- `POST /api/auth/register` - Create account
- `POST /api/auth/login` - Login
- `POST /api/auth/refresh` - Refresh token

### Email Verification

- `GET /api/auth/email-verification/status` - Check status
- `POST /api/auth/email-verification/verify` - Enter code
- `POST /api/auth/email-verification/resend` - Resend code (1 min limit)

### Password Reset

- `POST /api/auth/password-reset/request` - Request reset
- `POST /api/auth/password-reset/reset` - Reset with token

### Consent

- `GET /api/consent/status` - Get consent status
- `POST /api/consent/agree` - Save consent
- `GET /api/consent/history` - View consent history

### Health Profile

- `GET /api/profile` - Get profile
- `POST /api/profile` - Create/update profile (step-by-step)

### 2FA

- `POST /api/auth/2fa/setup/authenticator` - Get QR code
- `POST /api/auth/2fa/verify/authenticator` - Verify code
- `POST /api/auth/2fa/setup/sms` - Setup SMS
- `POST /api/auth/2fa/verify/sms` - Verify SMS
- `GET /api/auth/2fa/status` - Check 2FA status
- `POST /api/auth/2fa/disable` - Disable 2FA

## Frontend Routes

| Route            | Who                   | What                       |
| ---------------- | --------------------- | -------------------------- |
| `/auth/register` | Not logged in         | Register form              |
| `/auth/login`    | Not logged in         | Login form                 |
| `/verify-email`  | Logged in, unverified | Verify email code          |
| `/gdpr-consent`  | Logged in, no consent | GDPR form                  |
| `/setup/profile` | Logged in             | 5-step profile form        |
| `/setup/2fa`     | Logged in             | 2FA setup                  |
| `/dashboard`     | Logged in             | Main app                   |
| `/settings`      | Logged in             | Settings & consent history |

## Key Rules

### Database

- ✅ All migrations append-only (never modify old ones)
- ✅ Use `TIMESTAMPTZ` for all timestamps
- ✅ Use `UUID` for all IDs
- ✅ Foreign keys with `ON DELETE CASCADE`

### Java Backend

- ✅ Service layer has business logic (not controller)
- ✅ DTOs hide sensitive fields (no passwords in responses)
- ✅ Repositories query database (not controller or service)
- ✅ Global exception handler for all errors
- ✅ @Transactional for multi-step operations

### React Frontend

- ✅ Feature folders are independent
- ✅ api.ts has all HTTP calls (not in components)
- ✅ Custom hooks manage state (not useState everywhere)
- ✅ Components are UI-focused (not logic)
- ✅ Types defined in types.ts (not inline)

### Security

- ✅ Passwords: BCrypt hashing
- ✅ Tokens: httpOnly cookies (not localStorage)
- ✅ Access token: 15 min expiry
- ✅ Refresh token: 7 days expiry
- ✅ 2FA secrets: AES-256 encryption
- ✅ Rate limiting: Email resend 1x/min, login throttled

## Validation Rules

### Password

- Min 8 chars
- 1 uppercase
- 1 number
- 1 special char

### Height

- 100-250 cm

### Weight

- 0.1-300 kg

### Email

- Valid format
- Unique

## Phase Checkpoints

### Phase 1 Done When

- ✅ User can register
- ✅ Email verification code sent (6 digits, 24 hrs)
- ✅ Can resend once per minute
- ✅ User can verify email
- ✅ User can login
- ✅ JWT tokens work (access + refresh)
- ✅ Password reset works
- ✅ All tests pass

### Phase 2 Done When

- ✅ User must agree to GDPR consent
- ✅ Can't access health features without consent
- ✅ Can view consent history
- ✅ Consent timestamp stored

### Phase 3 Done When

- ✅ All 5 steps fillable
- ✅ Can skip steps
- ✅ Can save partial
- ✅ Data validation works
- ✅ Dashboard shows progress

### Phase 4 Done When

- ✅ Authenticator app setup works
- ✅ SMS setup works
- ✅ Backup codes generated
- ✅ 2FA optional (not required)

### Phase 5 Done When

- ✅ Dashboard shows checklist
- ✅ Alerts for incomplete items
- ✅ Email not verified → alert
- ✅ Profile incomplete → alert
- ✅ 2FA recommended → badge

### Phase 6 Done When

- ✅ Can log weight
- ✅ BMI calculated
- ✅ Weight history shown
- ✅ No duplicate same-day entries
- ✅ Trend chart works

---

## Remember

This is **your specification**. If something doesn't make sense, **say so now** before we code.

Each decision locked in:

- Email verification: soft gate ✓
- Code: 6 digits ✓
- Resend: once per minute ✓
- 2FA: optional, Authenticator + SMS ✓
- GDPR: hard gate ✓
- Profile: comprehensive, 5 steps ✓
- Auth: email/password only ✓
- Password reset: Phase 1 ✓
- Encryption: yes ✓
