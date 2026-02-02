# Technical Specification: Numbers Don't Lie Wellness App

**Status**: Design Phase Complete - Ready for Implementation
**Last Updated**: 2026-01-23
**Version**: 1.0

---

## Table of Contents

1. [User Flow & Journey](#user-flow--journey)
2. [Database Schema](#database-schema)
3. [Java Entity Classes](#java-entity-classes)
4. [API Contracts](#api-contracts)
5. [Frontend Component Structure](#frontend-component-structure)
6. [Implementation Phases](#implementation-phases)
7. [Design Patterns & Architecture](#design-patterns--architecture)

---

## User Flow & Journey

### Complete User Journey

```
START
  │
  ├─ Not Logged In?
  │  └─► REGISTER PAGE
  │      ├─ Enter: email, password
  │      ├─ Backend: hash password, create user, send verification email
  │      ├─ Return: JWT access token + refresh token
  │      └─► Navigate to: /verify-email
  │
  ├─ VERIFY EMAIL PAGE (SOFT GATE)
  │  ├─ State: email sent, code valid 24 hours
  │  ├─ Actions:
  │  │   ├─ Enter 6-digit code (from email)
  │  │   ├─ [Resend] button (rate limited: once per minute)
  │  │   └─ [Skip for now] (soft gate - can still use app)
  │  ├─ If verified: Mark user.email_verified = true
  │  └─► Navigate to: /gdpr-consent
  │
  ├─ GDPR CONSENT PAGE (HARD GATE - must be on path)
  │  ├─ Display: 2 consent boxes
  │  │   ├─ ☐ I consent to data collection for health analysis
  │  │   └─ ☐ I consent to AI-generated insights from my health data
  │  ├─ [Agree] button (disabled until both checked)
  │  ├─ [Disagree] button (logout, delete account)
  │  ├─ Backend: Store consent_records with timestamp
  │  └─► Navigate to: /setup/profile or /dashboard
  │
  ├─ PROFILE SETUP (OPTIONAL but RECOMMENDED)
  │  ├─ Multi-step form (5 steps)
  │  │   ├─ Step 1: Basic Info (birth_year, gender, consent)
  │  │   ├─ Step 2: Physical Metrics (height, current weight)
  │  │   ├─ Step 3: Lifestyle (occupation, activity level, sleep)
  │  │   ├─ Step 4: Fitness Assessment (exercise types, goals, preferences)
  │  │   └─ Step 5: Dietary (preferences, restrictions)
  │  ├─ Can skip after each step (shows "incomplete" on dashboard)
  │  ├─ Backend: Save to health_profiles table
  │  └─► Navigate to: /setup/2fa or /dashboard
  │
  ├─ 2FA SETUP (OPTIONAL but RECOMMENDED)
  │  ├─ Display: "Setup 2FA (Recommended)"
  │  ├─ Options: Authenticator App or SMS
  │  │   ├─ Authenticator: Show QR code, user scans, enters 6-digit code
  │  │   └─ SMS: User enters phone, confirms code sent via SMS
  │  ├─ Backend: Store encrypted secret or phone
  │  ├─ [Complete Setup] or [Skip for now]
  │  └─► Navigate to: /dashboard
  │
  └─ DASHBOARD (MAIN APP)
     ├─ Shows:
     │   ├─ Setup Progress checklist (with ✓ or ☐)
     │   ├─ Health Metrics (BMI, Wellness Score - if data available)
     │   ├─ Alerts if incomplete:
     │   │   ├─ Email not verified
     │   │   ├─ Profile missing data
     │   │   └─ 2FA recommended
     │   ├─ Weight history chart (if entries exist)
     │   └─ AI Insights (if complete)
     ├─ Navigation: Profile, Goals, Weight Log, Trends, Settings
     └─► Can access full app or go back to setup
```

### Entry Points by User State

| User State                    | URL             | Action                                 |
| ----------------------------- | --------------- | -------------------------------------- |
| Not logged in                 | `/`             | Show login page                        |
| Logged in, email not verified | `/verify-email` | Show email verification form           |
| Logged in, no GDPR consent    | `/gdpr-consent` | Show consent form (hard gate)          |
| Logged in, incomplete setup   | `/dashboard`    | Show dashboard with incomplete markers |
| Logged in, complete setup     | `/dashboard`    | Show full dashboard with all features  |

---

## Database Schema

### Migration File: `V4__auth_and_health_complete.sql`

```sql
-- ============================================
-- PHASE 1: USER AUTHENTICATION
-- ============================================

CREATE TABLE users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email TEXT NOT NULL UNIQUE,
  password_hash TEXT NOT NULL,
  email_verified BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMPTZ DEFAULT now(),
  updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_users_email ON users(email);

-- Email verification codes (6 digits, 24 hour expiry)
CREATE TABLE email_verification_codes (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  code TEXT NOT NULL, -- 6 digits
  created_at TIMESTAMPTZ DEFAULT now(),
  expires_at TIMESTAMPTZ NOT NULL, -- now() + 24 hours
  verified_at TIMESTAMPTZ, -- NULL until verified
  last_resent_at TIMESTAMPTZ -- For rate limiting (once per minute)
);

CREATE INDEX idx_email_codes_user ON email_verification_codes(user_id);
CREATE INDEX idx_email_codes_expires ON email_verification_codes(expires_at);

-- Password reset tokens (same pattern as email verification)
CREATE TABLE password_reset_tokens (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  token TEXT NOT NULL UNIQUE,
  created_at TIMESTAMPTZ DEFAULT now(),
  expires_at TIMESTAMPTZ NOT NULL, -- now() + 1 hour
  used_at TIMESTAMPTZ -- NULL until used
);

CREATE INDEX idx_pwd_reset_user ON password_reset_tokens(user_id);
CREATE INDEX idx_pwd_reset_token ON password_reset_tokens(token);

-- ============================================
-- PHASE 2: GDPR CONSENT
-- ============================================

CREATE TABLE consent_records (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  consent_type TEXT NOT NULL, -- "data_collection" or "ai_insights"
  version TEXT DEFAULT '1.0',
  agreed BOOLEAN NOT NULL,
  agreed_at TIMESTAMPTZ NOT NULL,
  ip_address TEXT,
  created_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_consent_user ON consent_records(user_id);

-- ============================================
-- PHASE 3: TWO FACTOR AUTHENTICATION
-- ============================================

CREATE TABLE two_factor_settings (
  user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
  method TEXT, -- "authenticator", "sms", NULL (disabled)
  secret_encrypted TEXT, -- For TOTP (encrypted at rest)
  sms_phone TEXT, -- For SMS method
  backup_codes_encrypted TEXT[], -- JSON array, encrypted
  is_enabled BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMPTZ DEFAULT now(),
  updated_at TIMESTAMPTZ DEFAULT now()
);

-- ============================================
-- PHASE 4: COMPREHENSIVE HEALTH PROFILE
-- ============================================

CREATE TABLE health_profiles (
  user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,

  -- STEP 1: BASIC INFO
  birth_year INT CHECK (birth_year >= 1900 AND birth_year <= YEAR(now())),
  gender TEXT CHECK (gender IN ('male', 'female', 'other')),

  -- STEP 2: PHYSICAL METRICS
  height_cm INT CHECK (height_cm >= 100 AND height_cm <= 250),
  current_weight_kg NUMERIC(5,2) CHECK (current_weight_kg > 0 AND current_weight_kg <= 300),
  body_type TEXT CHECK (body_type IN ('ectomorph', 'mesomorph', 'endomorph') OR body_type IS NULL),

  -- STEP 3: LIFESTYLE
  occupation_type TEXT, -- "office_worker", "manual_labor", "athlete", "student", etc.
  activity_level TEXT CHECK (
    activity_level IN ('sedentary', 'lightly_active', 'moderately_active', 'very_active', 'extra_active')
    OR activity_level IS NULL
  ),
  sleep_hours_per_night INT CHECK (sleep_hours_per_night >= 0 AND sleep_hours_per_night <= 24 OR sleep_hours_per_night IS NULL),

  -- STEP 4: FITNESS ASSESSMENT
  weekly_activity_days INT CHECK (weekly_activity_days >= 0 AND weekly_activity_days <= 7 OR weekly_activity_days IS NULL),
  exercise_types TEXT[], -- JSON: ["cardio", "strength", "flexibility", "sports"]
  session_duration TEXT CHECK (
    session_duration IN ('15_30min', '30_60min', '60plus_min')
    OR session_duration IS NULL
  ),
  fitness_level TEXT CHECK (fitness_level IN ('beginner', 'intermediate', 'advanced') OR fitness_level IS NULL),
  exercise_environment TEXT CHECK (exercise_environment IN ('home', 'gym', 'outdoors') OR exercise_environment IS NULL),
  exercise_time_preference TEXT CHECK (exercise_time_preference IN ('morning', 'afternoon', 'evening') OR exercise_time_preference IS NULL),
  endurance_level TEXT, -- Free text: "can run for 20 minutes" or NULL
  strength_level TEXT, -- Free text: "can do 15 pushups" or NULL

  -- STEP 5: DIETARY
  dietary_preferences TEXT[], -- JSON: ["vegetarian", "vegan", "paleo", "keto"]
  dietary_restrictions TEXT[], -- JSON: ["dairy_free", "gluten_free", "nut_allergy"]

  -- GOALS
  goal_type TEXT NOT NULL CHECK (goal_type IN ('weight_loss', 'muscle_gain', 'general_fitness')),
  target_weight_kg NUMERIC(5,2) CHECK (target_weight_kg > 0 AND target_weight_kg <= 300 OR target_weight_kg IS NULL),
  target_activity_level TEXT CHECK (
    target_activity_level IN ('lightly_active', 'moderately_active', 'very_active')
    OR target_activity_level IS NULL
  ),
  goal_timeline_weeks INT CHECK (goal_timeline_weeks > 0 OR goal_timeline_weeks IS NULL),

  -- Metadata
  created_at TIMESTAMPTZ DEFAULT now(),
  updated_at TIMESTAMPTZ DEFAULT now()
);

-- ============================================
-- PHASE 5: WEIGHT TRACKING
-- ============================================

CREATE TABLE weight_entries (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  weight_kg NUMERIC(5,2) NOT NULL CHECK (weight_kg > 0 AND weight_kg <= 300),
  measured_at TIMESTAMPTZ NOT NULL,
  note TEXT,
  created_at TIMESTAMPTZ DEFAULT now(),
  updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_weight_user_date ON weight_entries(user_id, measured_at DESC);
CREATE UNIQUE INDEX idx_weight_no_duplicate_date ON weight_entries(user_id, DATE(measured_at));

-- Prevent duplicate entries for same day
-- (If user tries to log weight twice same day, replace or reject)

-- ============================================
-- EXISTING: AI INSIGHTS
-- ============================================

-- (Keep existing ai_insights, audit_events tables as-is)

```

---

## Java Entity Classes

### 1. User Entity

```java
// Package: com.erikallas.ndl.user

@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "email_verified")
    private Boolean emailVerified = false;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    // Relationships
    @OneToOne(mappedBy = "user")
    private HealthProfileEntity healthProfile;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<EmailVerificationCodeEntity> verificationCodes;

    @OneToOne(mappedBy = "user")
    private TwoFactorSettingsEntity twoFactorSettings;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<ConsentRecordEntity> consentRecords;

    // Getters/setters...
}
```

### 2. Email Verification Code Entity

```java
// Package: com.erikallas.ndl.auth

@Entity
@Table(name = "email_verification_codes")
public class EmailVerificationCodeEntity {
    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(nullable = false)
    private String code; // 6 digits

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt; // 24 hours from creation

    @Column(name = "verified_at")
    private OffsetDateTime verifiedAt;

    @Column(name = "last_resent_at")
    private OffsetDateTime lastResentAt;

    // Methods
    public boolean isExpired() {
        return OffsetDateTime.now().isAfter(expiresAt);
    }

    public boolean isAlreadyVerified() {
        return verifiedAt != null;
    }

    public boolean canResend() {
        if (lastResentAt == null) return true;
        return OffsetDateTime.now().isAfter(lastResentAt.plusMinutes(1));
    }
}
```

### 3. Health Profile Entity

```java
// Package: com.erikallas.ndl.health.profile

@Entity
@Table(name = "health_profiles")
public class HealthProfileEntity {
    @Id
    private UUID userId; // FK to users

    @OneToOne
    @MapsId
    private UserEntity user;

    // Step 1: Basic
    private Integer birthYear;
    private String gender; // male, female, other

    // Step 2: Physical
    private Integer heightCm;
    private BigDecimal currentWeightKg;
    private String bodyType; // ectomorph, mesomorph, endomorph

    // Step 3: Lifestyle
    private String occupationType;
    private String activityLevel; // sedentary, lightly_active, etc.
    private Integer sleepHoursPerNight;

    // Step 4: Fitness Assessment
    private Integer weeklyActivityDays;

    @ElementCollection
    @CollectionTable(name = "health_profile_exercise_types")
    private Set<String> exerciseTypes; // cardio, strength, flexibility, sports

    private String sessionDuration; // 15_30min, 30_60min, 60plus_min
    private String fitnessLevel; // beginner, intermediate, advanced
    private String exerciseEnvironment; // home, gym, outdoors
    private String exerciseTimePreference; // morning, afternoon, evening
    private String enduranceLevel; // Free text
    private String strengthLevel; // Free text

    // Step 5: Dietary
    @ElementCollection
    @CollectionTable(name = "health_profile_dietary_prefs")
    private Set<String> dietaryPreferences;

    @ElementCollection
    @CollectionTable(name = "health_profile_dietary_restrictions")
    private Set<String> dietaryRestrictions;

    // Goals
    private String goalType; // weight_loss, muscle_gain, general_fitness
    private BigDecimal targetWeightKg;
    private String targetActivityLevel;
    private Integer goalTimelineWeeks;

    // Metadata
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
```

### 4. Consent Record Entity

```java
// Package: com.erikallas.ndl.consent

@Entity
@Table(name = "consent_records")
public class ConsentRecordEntity {
    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    private String consentType; // data_collection, ai_insights
    private String version;
    private Boolean agreed;
    private OffsetDateTime agreedAt;
    private String ipAddress;
    private OffsetDateTime createdAt;
}
```

### 5. Two Factor Settings Entity

```java
// Package: com.erikallas.ndl.auth.twofactor

@Entity
@Table(name = "two_factor_settings")
public class TwoFactorSettingsEntity {
    @Id
    private UUID userId;

    @OneToOne
    @MapsId
    private UserEntity user;

    private String method; // authenticator, sms, null
    private String secretEncrypted; // TOTP secret (encrypted)
    private String smsPhone;

    @ElementCollection
    @CollectionTable(name = "two_factor_backup_codes")
    private List<String> backupCodesEncrypted;

    private Boolean isEnabled = false;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
```

---

## API Contracts

### Authentication Endpoints

#### Register User

```
POST /api/auth/register
Content-Type: application/json

Request:
{
  "email": "erik@example.com",
  "password": "SecurePassword123!"
}

Response (201 Created):
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "erik@example.com",
    "emailVerified": false,
    "createdAt": "2026-01-23T20:07:00Z"
  }
}

Error (400 Bad Request):
{
  "message": "Email already registered",
  "fieldErrors": {
    "email": "This email is already in use"
  }
}

Error (422 Unprocessable Entity):
{
  "fieldErrors": {
    "email": "Invalid email format",
    "password": "Password must be at least 8 characters with uppercase, number, and symbol"
  }
}
```

#### Login User

```
POST /api/auth/login
Content-Type: application/json

Request:
{
  "email": "erik@example.com",
  "password": "SecurePassword123!"
}

Response (200 OK):
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "erik@example.com",
    "emailVerified": true
  }
}

Error (401 Unauthorized):
{
  "message": "Invalid email or password"
}
```

#### Refresh Token

```
POST /api/auth/refresh
Content-Type: application/json

Request:
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}

Response (200 OK):
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

### Email Verification Endpoints

#### Get Verification Status

```
GET /api/auth/email-verification/status
Authorization: Bearer {token}

Response (200 OK):
{
  "email": "erik@example.com",
  "verified": false,
  "codeExpiresAt": "2026-01-24T20:07:00Z"
}
```

#### Verify Email Code

```
POST /api/auth/email-verification/verify
Authorization: Bearer {token}
Content-Type: application/json

Request:
{
  "code": "123456"
}

Response (200 OK):
{
  "verified": true
}

Error (400 Bad Request):
{
  "message": "Invalid or expired code"
}
```

#### Resend Verification Code

```
POST /api/auth/email-verification/resend
Authorization: Bearer {token}

Response (200 OK):
{
  "success": true,
  "message": "Verification code sent",
  "expiresAt": "2026-01-24T20:07:00Z"
}

Error (429 Too Many Requests):
{
  "message": "Too many resend attempts. Please wait 1 minute before trying again."
}
```

### Password Reset Endpoints

#### Request Password Reset

```
POST /api/auth/password-reset/request
Content-Type: application/json

Request:
{
  "email": "erik@example.com"
}

Response (200 OK):
{
  "message": "If email exists, password reset link has been sent"
}

Note: Always return 200 even if email doesn't exist (security)
```

#### Reset Password with Token

```
POST /api/auth/password-reset/reset
Content-Type: application/json

Request:
{
  "token": "reset-token-from-email",
  "newPassword": "NewPassword123!"
}

Response (200 OK):
{
  "message": "Password reset successful"
}

Error (400 Bad Request):
{
  "message": "Token expired or invalid"
}
```

### GDPR Consent Endpoints

#### Get Consent Status

```
GET /api/consent/status
Authorization: Bearer {token}

Response (200 OK):
{
  "dataCollectionConsent": {
    "agreed": true,
    "agreedAt": "2026-01-23T20:07:00Z"
  },
  "aiInsightsConsent": {
    "agreed": true,
    "agreedAt": "2026-01-23T20:07:00Z"
  }
}
```

#### Submit Consent

```
POST /api/consent/agree
Authorization: Bearer {token}
Content-Type: application/json

Request:
{
  "dataCollection": true,
  "aiInsights": true
}

Response (200 OK):
{
  "dataCollectionConsent": {
    "agreed": true,
    "agreedAt": "2026-01-23T20:07:00Z"
  },
  "aiInsightsConsent": {
    "agreed": true,
    "agreedAt": "2026-01-23T20:07:00Z"
  }
}
```

#### View Consent History

```
GET /api/consent/history
Authorization: Bearer {token}

Response (200 OK):
{
  "consents": [
    {
      "consentType": "data_collection",
      "agreed": true,
      "agreedAt": "2026-01-23T20:07:00Z",
      "version": "1.0"
    }
  ]
}
```

### Health Profile Endpoints

#### Get Profile

```
GET /api/profile
Authorization: Bearer {token}

Response (200 OK):
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "birthYear": 1992,
  "gender": "male",
  "heightCm": 180,
  "currentWeightKg": 85.5,
  // ... all other fields
  "createdAt": "2026-01-23T20:07:00Z",
  "updatedAt": "2026-01-23T20:07:00Z"
}

Error (404 Not Found):
{
  "message": "Profile not found"
}
```

#### Create/Update Profile (Step-by-step)

```
POST /api/profile
Authorization: Bearer {token}
Content-Type: application/json

Request (can be partial):
{
  "step": 1,
  "data": {
    "birthYear": 1992,
    "gender": "male"
  }
}

Response (200 OK):
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "birthYear": 1992,
  "gender": "male",
  // ... other fields (null if not provided)
}
```

### 2FA Endpoints

#### Setup Authenticator

```
POST /api/auth/2fa/setup/authenticator
Authorization: Bearer {token}

Response (200 OK):
{
  "secret": "JBSWY3DPEBLW64TMMQ======",
  "qrCodeUrl": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA..."
}
```

#### Verify Authenticator Code

```
POST /api/auth/2fa/verify/authenticator
Authorization: Bearer {token}
Content-Type: application/json

Request:
{
  "code": "123456"
}

Response (200 OK):
{
  "enabled": true,
  "backupCodes": ["XXXX-XXXX", "XXXX-XXXX", ...]
}
```

#### Setup SMS 2FA

```
POST /api/auth/2fa/setup/sms
Authorization: Bearer {token}
Content-Type: application/json

Request:
{
  "phone": "+1234567890"
}

Response (200 OK):
{
  "message": "SMS code sent to +1234567890"
}
```

#### Verify SMS Code

```
POST /api/auth/2fa/verify/sms
Authorization: Bearer {token}
Content-Type: application/json

Request:
{
  "code": "123456"
}

Response (200 OK):
{
  "enabled": true,
  "backupCodes": ["XXXX-XXXX", "XXXX-XXXX", ...]
}
```

#### Get 2FA Status

```
GET /api/auth/2fa/status
Authorization: Bearer {token}

Response (200 OK):
{
  "enabled": true,
  "method": "authenticator",
  "setupAt": "2026-01-23T20:07:00Z"
}
```

#### Disable 2FA

```
POST /api/auth/2fa/disable
Authorization: Bearer {token}

Response (200 OK):
{
  "enabled": false
}
```

---

## Frontend Component Structure

```
src/
  app/
    App.tsx                      # Main router
    Layout.tsx                   # Wrapper with auth check

  features/
    auth/
      pages/
        RegisterPage.tsx
        LoginPage.tsx
        VerifyEmailPage.tsx
        PasswordResetPage.tsx
        PasswordResetRequestPage.tsx
      components/
        RegisterForm.tsx
        LoginForm.tsx
        VerifyEmailForm.tsx
      api.ts                     # register(), login(), verifyEmail(), etc.
      hooks/
        useAuth.ts
        useAuthCheck.ts

    consent/
      pages/
        GdprConsentPage.tsx
      components/
        ConsentForm.tsx
      api.ts
      hooks/
        useConsentStatus.ts

    setup/
      pages/
        ProfileSetupPage.tsx     # Multi-step wizard
        TwoFactorSetupPage.tsx
      components/
        ProfileStep1.tsx         # Basic info
        ProfileStep2.tsx         # Physical
        ProfileStep3.tsx         # Lifestyle
        ProfileStep4.tsx         # Fitness
        ProfileStep5.tsx         # Dietary
        TwoFactorForm.tsx
        SetupProgress.tsx        # Shows checkmarks
      api.ts

    dashboard/
      pages/
        DashboardPage.tsx
      components/
        SetupChecklist.tsx       # Shows ✓ or ☐
        CompletionAlerts.tsx     # Email verify, 2FA recommended, etc.
        HealthMetrics.tsx
        WeightTrend.tsx
      api.ts

  shared/
    api/
      client.ts
      types.ts                   # Shared types
    auth/
      useAuthToken.ts
      useLogout.ts
      ProtectedRoute.tsx         # Require token
      ConsentGate.tsx            # Require GDPR consent
    ui/
      Button.tsx
      Card.tsx
      Alert.tsx
      Input.tsx
      Spinner.tsx
      Modal.tsx
      etc.
```

---

## Implementation Phases

### Phase 1: User Authentication (1 week)

**Goal**: Users can register, login, verify email, reset password

**Backend**:

- [ ] UserEntity class
- [ ] UserRepository with queries
- [ ] PasswordEncoder configuration (BCrypt)
- [ ] JWT token generation/validation
- [ ] AuthController: register, login, refresh
- [ ] EmailVerificationCodeEntity & repository
- [ ] Email service (send verification codes)
- [ ] EmailVerificationController: verify, resend
- [ ] PasswordResetToken entity & service
- [ ] PasswordResetController: request, reset
- [ ] Tests for auth flows

**Frontend**:

- [ ] RegisterPage component
- [ ] LoginPage component
- [ ] VerifyEmailPage component
- [ ] PasswordResetRequestPage component
- [ ] PasswordResetPage component
- [ ] useAuth hook
- [ ] API client module for auth

**Database**:

- [ ] Migration: users, email_verification_codes, password_reset_tokens

**Acceptance Criteria**:

- User can register with email/password
- Verification code sent to email
- User can verify email with code
- 24-hour expiry enforced
- Resend button works with 1-minute rate limit
- User can reset password
- JWT tokens work
- Login returns tokens
- Token refresh works

---

### Phase 2: GDPR Consent (3 days)

**Goal**: Users must agree to terms before using health features

**Backend**:

- [ ] ConsentRecordEntity & repository
- [ ] ConsentController: getStatus, agree, getHistory
- [ ] ConsentService with validation
- [ ] Store consent with timestamp & IP address

**Frontend**:

- [ ] GdprConsentPage component
- [ ] ConsentForm with 2 checkboxes
- [ ] useConsentStatus hook
- [ ] Redirect to consent page if not agreed

**Database**:

- [ ] Add to migration: consent_records table

**Acceptance Criteria**:

- User sees consent page after email verification
- Can't proceed without both checkboxes checked
- [Agree] button saves consent with timestamp
- Can view consent history in Settings page
- Consent status tracked in database

---

### Phase 3: Health Profile Setup (2 weeks)

**Goal**: Collect comprehensive health data in 5 steps

**Backend**:

- [ ] HealthProfileEntity with all fields
- [ ] HealthProfileRepository
- [ ] HealthProfileService: validation, save partial data
- [ ] ProfileController: get, create, update by step
- [ ] Validation rules for each step

**Frontend**:

- [ ] ProfileSetupPage (multi-step wizard)
- [ ] ProfileStep1 (basic info)
- [ ] ProfileStep2 (physical metrics)
- [ ] ProfileStep3 (lifestyle)
- [ ] ProfileStep4 (fitness assessment)
- [ ] ProfileStep5 (dietary)
- [ ] SetupProgress component (shows which steps complete)
- [ ] Can skip after each step, resume later

**Database**:

- [ ] Add to migration: health_profiles table

**Acceptance Criteria**:

- All 5 steps can be filled
- Data validated (height 100-250cm, weight 30-300kg, etc.)
- Can save partial (skip steps)
- Can update profile after saving
- Dashboard shows which steps are complete/incomplete
- GDPR consent required to save health data

---

### Phase 4: 2FA Setup (1 week)

**Goal**: Users can optionally enable 2FA (Authenticator or SMS)

**Backend**:

- [ ] TwoFactorSettingsEntity & repository
- [ ] TOTP library (time-based one-time password)
- [ ] TwoFactorService: generate secret, verify code, backup codes
- [ ] 2FAController: setup, verify, status, disable
- [ ] Encryption for stored secrets
- [ ] SMS provider integration (Twilio or similar)

**Frontend**:

- [ ] TwoFactorSetupPage
- [ ] TwoFactorForm with Authenticator/SMS choice
- [ ] QR code display (for authenticator)
- [ ] Code verification form
- [ ] Backup codes display & download
- [ ] 2FA status page in Settings

**Database**:

- [ ] Add to migration: two_factor_settings table

**Acceptance Criteria**:

- User can set up authenticator app
- QR code generated correctly
- User must enter valid code to enable
- Backup codes generated (10-15 codes)
- SMS option works
- 2FA status shown on dashboard
- Can disable 2FA
- Encrypted storage

---

### Phase 5: Dashboard & Setup Flow (1 week)

**Goal**: Dashboard shows setup progress and alerts

**Frontend**:

- [ ] DashboardPage layout
- [ ] SetupChecklist component (shows ✓ or ☐)
- [ ] CompletionAlerts component (email verify, profile, 2FA)
- [ ] HealthMetrics display (BMI - calculated, Wellness score - TBD)
- [ ] Weight trend (if data exists)
- [ ] Navigation routing based on user state
- [ ] Loading states & error handling

**Backend**:

- [ ] Dashboard info endpoint: /api/dashboard/status
- [ ] Returns: profile completeness, alerts, basic metrics

**Acceptance Criteria**:

- Dashboard shows setup checklist
- Email not verified → alert with [Verify] button
- Profile incomplete → alert with [Complete Profile] button
- 2FA not set → "Recommended" badge
- All features accessible but gracefully degraded if incomplete
- Redirect flows work (register → verify → consent → profile → dashboard)

---

### Phase 6: Weight Tracking & Analytics (2 weeks)

**Goal**: Users can log weight and see trends (ties into wellness score)

**Backend**:

- [ ] WeightEntryEntity & repository
- [ ] WeightService: log, get history, calculate trends
- [ ] WeightController: list, create
- [ ] BMI calculation service
- [ ] Prevent duplicate entries for same day

**Frontend**:

- [ ] CheckIn page to log weight
- [ ] Weight history component
- [ ] Weight trend chart (Chart.js or similar)
- [ ] Goal progress visualization

**Database**:

- [ ] weight_entries table (already have, just add unique constraint)

**Acceptance Criteria**:

- User can log weight with date
- Weight history shown with timestamps
- Can't log duplicate for same day
- BMI calculated correctly
- Chart shows trend over time

---

## Design Patterns & Architecture

### Backend Patterns

#### 1. Service-Repository Pattern

```java
// Repository: Data access
public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmail(String email);
}

// Service: Business logic
@Service
public class AuthService {
    private final UserRepository userRepo;
    private final PasswordEncoder pwEncoder;

    public UserEntity register(String email, String password) {
        // Validate
        if (userRepo.findByEmail(email).isPresent()) {
            throw new EmailAlreadyRegisteredException();
        }

        // Hash password
        String hash = pwEncoder.encode(password);

        // Save
        UserEntity user = new UserEntity(email, hash);
        return userRepo.save(user);
    }
}

// Controller: HTTP handling
@RestController
public class AuthController {
    private final AuthService authService;

    @PostMapping("/api/auth/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        UserEntity user = authService.register(req.getEmail(), req.getPassword());
        return ResponseEntity.ok(user);
    }
}
```

**Key principle**: Controllers don't do business logic. Services contain logic. Repositories access data.

#### 2. DTO Pattern

```java
// Entity (database representation)
@Entity
public class UserEntity {
    private String passwordHash;
}

// DTO (what API returns - NEVER expose passwordHash)
public class UserDto {
    public String id;
    public String email;
    public boolean emailVerified;
}

// Controller
public UserDto register(...) {
    UserEntity user = authService.register(...);
    return new UserDto(user.getId(), user.getEmail(), user.isEmailVerified());
}
```

**Key principle**: Never leak sensitive fields (passwords, encrypted secrets, etc.)

#### 3. Error Handling

```java
// Custom exception
public class EmailAlreadyRegisteredException extends RuntimeException {
    public EmailAlreadyRegisteredException() {
        super("Email already registered");
    }
}

// Global exception handler
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    public ResponseEntity<?> handleEmailAlreadyRegistered(EmailAlreadyRegisteredException e) {
        ApiError error = new ApiError(
            HttpStatus.BAD_REQUEST.value(),
            "Email already registered",
            Map.of("email", "This email is already in use")
        );
        return ResponseEntity.badRequest().body(error);
    }
}
```

### Frontend Patterns

#### 1. Custom Hooks for Data Fetching

```typescript
// Hook: manages loading, error, data states
function useAuth() {
    const [user, setUser] = useState<UserDto | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<Error | null>(null);

    const register = async (email: string, password: string) => {
        setLoading(true);
        setError(null);
        try {
            const response = await api.post('/api/auth/register', { email, password });
            setUser(response.user);
            localStorage.setItem('token', response.token);
        } catch (err) {
            setError(err as Error);
        } finally {
            setLoading(false);
        }
    };

    return { user, loading, error, register };
}

// Component: uses hook
function RegisterPage() {
    const { user, loading, error, register } = useAuth();

    const handleSubmit = async (email, password) => {
        await register(email, password);
    };

    if (loading) return <Spinner />;
    if (error) return <Alert type="error" message={error.message} />;
    if (user) return <Navigate to="/verify-email" />;

    return <RegisterForm onSubmit={handleSubmit} />;
}
```

#### 2. Feature-Based Folder Structure

Each feature is self-contained:

- `pages/`: Page-level components
- `components/`: Smaller components within feature
- `api.ts`: All API calls for this feature
- `hooks/`: Feature-specific hooks
- `types.ts`: TypeScript types for feature

#### 3. Separation of Concerns

```typescript
// ✅ Component focused on UI
function RegisterForm({ onSubmit }: { onSubmit: (email, password) => void }) {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');

    return (
        <form onSubmit={() => onSubmit(email, password)}>
            <TextField value={email} onChange={setEmail} />
            <TextField type="password" value={password} onChange={setPassword} />
            <Button type="submit">Register</Button>
        </form>
    );
}

// ✅ Page handles logic & state
function RegisterPage() {
    const { user, loading, error, register } = useAuth();

    const handleSubmit = async (email, password) => {
        await register(email, password);
        if (user) navigate('/verify-email');
    };

    return <RegisterForm onSubmit={handleSubmit} />;
}
```

---

## Validation Rules

### Backend Validation

```
Email:
  - Valid email format (RFC 5322)
  - Unique (not already registered)
  - Lowercase stored

Password:
  - Minimum 8 characters
  - At least 1 uppercase letter
  - At least 1 number
  - At least 1 special character (!@#$%^&*)

Height:
  - 100-250 cm

Weight:
  - 0.1-300 kg

Age:
  - Birth year 1900-present

Activity Days:
  - 0-7
```

### Frontend Validation

Real-time feedback as user types, backend confirms on submit.

---

## Security Considerations

1. **Passwords**: BCrypt hashing (Spring Security default)
2. **Tokens**:
   - Access token: 15 minutes expiry
   - Refresh token: 7 days expiry
   - Stored in httpOnly cookie (not localStorage)
3. **2FA Secrets**: AES-256 encryption at rest
4. **GDPR Consent**: Immutable record with timestamp & IP
5. **Email Codes**: 6 digits, 24-hour expiry, one-time use
6. **Password Reset**: Token-based, 1-hour expiry, one-time use
7. **Rate Limiting**: Resend email once per minute, login attempts throttled
8. **HTTPS**: Required in production (enforced by Spring Security)

---

## Testing Strategy

Each phase will have:

1. **Unit tests** (services, utils)
2. **Integration tests** (controller + service + repository)
3. **E2E tests** (full flow, frontend + backend)

Example:

```java
@Test
void testRegisterWithValidCredentials() {
    String response = authService.register("erik@test.com", "Password123!");
    assertThat(response).isNotNull();
    assertThat(userRepository.findByEmail("erik@test.com")).isPresent();
}

@Test
void testRegisterWithDuplicateEmail() {
    authService.register("erik@test.com", "Password123!");
    assertThrows(EmailAlreadyRegisteredException.class,
        () -> authService.register("erik@test.com", "AnotherPassword123!"));
}
```

---

## Summary: Ready for Implementation

This specification is **complete and unambiguous**. Before we code each phase:

1. ✅ Read the relevant section
2. ✅ Understand the entity structure
3. ✅ Understand the API contracts
4. ✅ Implement following the patterns
5. ✅ Test the phase
6. ✅ Move to next phase

**No surprises, no spaghetti.**
