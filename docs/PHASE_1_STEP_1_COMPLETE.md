# Phase 1 - Step 1 Complete: Database & Entity Classes

**Created Files**:

1. ✅ `V4__auth_and_passwords.sql` - Database migration
2. ✅ `UserEntity.java` - User model
3. ✅ `EmailVerificationCodeEntity.java` - Email verification code model
4. ✅ `PasswordResetTokenEntity.java` - Password reset token model
5. ✅ `UserRepository.java` - User data access
6. ✅ `EmailVerificationCodeRepository.java` - Email code data access
7. ✅ `PasswordResetTokenRepository.java` - Reset token data access

---

## What Each File Does

### Database Migration (V4)

- Creates 3 tables: `users`, `email_verification_codes`, `password_reset_tokens`
- Adds indexes for fast queries
- Includes documentation comments

**Key Constraints**:

- Email must be unique
- Passwords stored as hashes (never plain text)
- Codes are one-time use (verified_at marks completion)
- Tokens are one-time use (used_at marks completion)
- Codes expire in 24 hours
- Tokens expire in 1 hour

### Entities

Each entity has:

- **Fields**: Exactly what the DB table has
- **Constructor**: To create new instances
- **Getters/Setters**: To read/modify fields
- **Business Logic Methods**: Helper methods like `isExpired()`, `canResend()`, `isValid()`

**Why business logic in entities?**

- These methods are about the entity itself
- Example: `code.isExpired()` is clearer than `util.isExpired(code)`
- Keeps related logic together

### Repositories

These are **Spring Data JPA interfaces**. You declare what queries you need, Spring implements them automatically.

**How it works**:

```java
// You write this:
Optional<UserEntity> findByEmailIgnoreCase(String email);

// Spring generates this automatically:
// SELECT * FROM users WHERE LOWER(email) = LOWER(?)
```

**Methods we created**:

- `findByEmailIgnoreCase()` - Login & registration check
- `existsByEmailIgnoreCase()` - Check if email already taken
- `findByCode()` - Verify email code
- `findByUserIdAndVerifiedAtIsNull()` - Get unverified codes
- `findByToken()` - Reset password with token

---

## Ready for Step 2?

Next step is **Services** (business logic layer).

Services will:

- ✅ Validate inputs (email format, password strength, etc.)
- ✅ Handle errors clearly
- ✅ Use repositories to save/find data
- ✅ Generate codes & tokens
- ✅ Hash passwords with BCrypt

**Examples of what services do**:

```java
// AuthService
public UserEntity register(String email, String password) {
    // 1. Validate email format
    // 2. Check if email already exists
    // 3. Validate password strength
    // 4. Hash password with BCrypt
    // 5. Create user
    // 6. Generate verification code
    // 7. Return user
}

public void verifyEmail(UUID userId, String code) {
    // 1. Find user
    // 2. Find code
    // 3. Check code not expired
    // 4. Check code not already used
    // 5. Compare code from form with code in DB
    // 6. Mark code as verified
    // 7. Mark user.email_verified = true
}
```

---

## Questions Before Services?

Do you understand:

- ✅ The database structure (3 tables, indexes)?
- ✅ What entities are (JPA models)?
- ✅ Why we have business logic methods in entities?
- ✅ What repositories do (Spring Data JPA)?

If yes, say **"continue"** and I'll create the services.

If no, ask questions! 🙂
