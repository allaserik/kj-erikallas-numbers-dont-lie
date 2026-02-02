# Backend Folder Structure Review & Recommendations

## Current Issues

### 1. **Duplicate Entities in `/user/` Package**

**Problem**: Both `AppUserEntity.java` and `UserEntity.java` exist (leftover from refactoring)

- `AppUserEntity` - old Auth0-focused version
- `UserEntity` - new dual-auth version (should be the only one)

**Impact**: Code confusion, potential import issues, unnecessary files

**Solution**: Delete `AppUserEntity.java` and `AppUserRepository.java`

---

### 2. **Controllers in `/api/` Package Mixing Concerns**

**Current Structure**:

```
api/
├── AuthController.java           (authentication)
├── EmailVerificationController.java (email verification)
├── PasswordResetController.java   (password reset)
├── MeController.java             (user profile - Auth0)
├── PingController.java           (health check)
├── ValidationTestController.java (testing)
├── GlobalExceptionHandler.java   (error handling)
└── ApiError.java                 (error model)
```

**Problem**:

- Mixing all controllers in one package loses feature context
- GlobalExceptionHandler and ApiError should be in `config/` or `common/`
- No clear separation between features

**Best Practice**: Organize by feature, not by technical layer

---

## Recommended Architecture

### Option A: Feature-Based (Recommended) ⭐

```
backend/src/main/java/com/erikallas/ndl/
├── api/                          (REST API entry points)
│   ├── auth/
│   │   ├── AuthController.java
│   │   ├── AuthRequest.java
│   │   └── AuthResponse.java
│   ├── email-verification/
│   │   ├── EmailVerificationController.java
│   │   ├── VerifyEmailRequest.java
│   │   └── VerifyEmailResponse.java
│   ├── password-reset/
│   │   ├── PasswordResetController.java
│   │   ├── ResetPasswordRequest.java
│   │   └── ResetPasswordResponse.java
│   └── user/                     (User profile, Auth0 endpoints)
│       └── MeController.java
│
├── auth/                         (Feature: Authentication)
│   ├── service/
│   │   ├── AuthService.java
│   │   ├── EmailService.java
│   │   └── PasswordResetService.java
│   └── config/
│       └── PasswordEncoderConfig.java
│
├── user/                         (Feature: User Management)
│   ├── model/                    (Entities + Repositories)
│   │   ├── UserEntity.java
│   │   ├── UserRepository.java
│   │   ├── EmailVerificationCodeEntity.java
│   │   ├── EmailVerificationCodeRepository.java
│   │   ├── PasswordResetTokenEntity.java
│   │   └── PasswordResetTokenRepository.java
│   └── service/
│       └── UserService.java
│
├── health/                       (Feature: Health Tracking)
│   ├── model/
│   ├── service/
│   └── api/                      (Controllers for health endpoints)
│
├── common/                       (Shared utilities)
│   ├── error/
│   │   ├── ApiError.java
│   │   ├── GlobalExceptionHandler.java
│   │   └── ErrorResponse.java
│   └── health/
│       └── PingController.java
│
├── config/                       (Configuration beans)
│   ├── SecurityConfig.java
│   ├── PasswordEncoderConfig.java
│   └── JpaConfig.java
│
└── Application.java
```

### Why Option A is Better:

✅ **Feature-Focused**: Each feature (auth, user, health) is self-contained  
✅ **Clear Separation**: Controllers, Services, and Models grouped logically  
✅ **Scalability**: Easy to add new features without cluttering existing packages  
✅ **Consistency**: Every feature follows the same pattern  
✅ **Imports**: Clear import paths indicate dependencies  
✅ **Testing**: Test files mirror this structure

---

## Migration Path (Before Commit)

### Step 1: Fix Duplicates

```bash
rm backend/src/main/java/com/erikallas/ndl/user/AppUserEntity.java
rm backend/src/main/java/com/erikallas/ndl/user/AppUserRepository.java
```

### Step 2: Reorganize API Controllers

**Move & organize controllers**:

```
api/
├── auth/
│   ├── AuthController.java
│   ├── AuthRequest.java (extract DTOs)
│   └── AuthResponse.java
├── email-verification/
│   └── EmailVerificationController.java
├── password-reset/
│   └── PasswordResetController.java
└── user/
    └── MeController.java

common/
├── error/
│   ├── ApiError.java
│   └── GlobalExceptionHandler.java
└── controller/
    └── PingController.java
```

**Note**: Keep `ValidationTestController.java` for now (testing tool)

### Step 3: Reorganize Services & Config

**Move to `auth/service/`**:

- AuthService.java
- EmailService.java
- PasswordResetService.java

**Move to `auth/config/`**:

- PasswordEncoderConfig.java

**Move to `user/model/`**:

- UserEntity.java
- UserRepository.java
- EmailVerificationCodeEntity.java
- EmailVerificationCodeRepository.java
- PasswordResetTokenEntity.java
- PasswordResetTokenRepository.java

**Keep in `user/service/`**:

- UserService.java

### Step 4: Update Package Imports

After moving files, update all imports across the codebase.

---

## Summary of Changes

| Current                            | Recommended                               | Reason                |
| ---------------------------------- | ----------------------------------------- | --------------------- |
| `/api/AuthController.java`         | `/api/auth/AuthController.java`           | Group by feature      |
| `/api/ApiError.java`               | `/common/error/ApiError.java`             | Shared error handling |
| `/auth/PasswordEncoderConfig.java` | `/auth/config/PasswordEncoderConfig.java` | Config within feature |
| `/user/AppUserEntity.java`         | DELETE                                    | Duplicate entity      |
| `/user/AppUserRepository.java`     | DELETE                                    | Duplicate repository  |

---

## Files to Extract from Controllers (DTOs)

Current `AuthController` has inline DTOs. Better to extract:

```java
// api/auth/AuthRequest.java
public class AuthRegisterRequest { ... }
public class AuthLoginRequest { ... }

// api/auth/AuthResponse.java
public class AuthResponse { ... }
```

Same for other controllers to keep them clean.

---

## Recommendation

**Proceed with Option A** - It's the cleanest and most scalable approach. All three layers (API, Service, Model) are clearly separated within feature packages.

Should I help you reorganize the folders now before you commit?
