# Phase 1 - Important Update: Supporting Both Auth0 AND Email/Password

**Status**: Design revised to support BOTH authentication methods

---

## The Strategy

We're NOT replacing Auth0. We're **adding email/password alongside it**:

```
USERS TABLE (already exists from V2)
├── Existing Auth0 users:
│   ├── auth0_sub: "google-oauth2|123" ✓
│   ├── email: NULL (we get from Auth0 if needed)
│   ├── password_hash: NULL
│   └── email_verified: true (Auth0 verifies)
│
└── New Email/Password users:
    ├── auth0_sub: NULL
    ├── email: "erik@example.com" ✓
    ├── password_hash: "bcrypt$2a$12$..." ✓
    └── email_verified: false → true (after code entry)
```

### Database Changes (V4 Migration)

Instead of creating a new table, we **ALTER the existing users table**:

```sql
ALTER TABLE users
ADD COLUMN IF NOT EXISTS email TEXT UNIQUE,
ADD COLUMN IF NOT EXISTS password_hash TEXT,
ADD COLUMN IF NOT EXISTS email_verified BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ DEFAULT now();
```

**Why?**

- ✅ Existing Auth0 data untouched
- ✅ New email/password fields optional
- ✅ Both auth methods coexist
- ✅ Can migrate Auth0 users to email/password if needed later

---

## How It Works in Code

### Registering Auth0 User (Existing)

```java
// Auth0 users created by existing code
UserEntity user = new UserEntity(
    UUID.randomUUID(),
    "google-oauth2|1234567890"  // auth0_sub
);
// Fields: auth0_sub="google-oauth2|1234567890", email=null, password_hash=null, email_verified=true
```

### Registering Email/Password User (New)

```java
// Email/password users created by our new auth service
UserEntity user = new UserEntity(
    UUID.randomUUID(),
    "erik@example.com",           // email
    "bcrypt_hash_here"            // password_hash
);
// Fields: auth0_sub=null, email="erik@example.com", password_hash="bcrypt...", email_verified=false
```

### Checking User Type

```java
// Helper methods in UserEntity
if (user.isAuth0User()) {
    // Use Auth0 flow
} else if (user.isEmailPasswordUser()) {
    // Use email/password flow
}
```

---

## Updated UserEntity Class Needed

The UserEntity needs to be updated to:

1. Include `auth0_sub` field (it was missing!)
2. Make `email` and `password_hash` optional (nullable)
3. Make `email_verified` only relevant for email users
4. Add helper methods: `isAuth0User()`, `isEmailPasswordUser()`

**Current State**: The entity currently assumes email/password only
**Needed**: Update to support both

---

## Service Layer Considerations

### AuthService for Email/Password

Only handles email/password users:

```java
public UserEntity registerEmailPassword(String email, String password) {
    // Validate
    // Hash password
    // Create user with email + password_hash
    // Send verification email
    // Generate code
}
```

### Existing Auth0 Flow (Untouched)

The existing `UserService.ensureUser(auth0Sub, email)` continues to work:

```java
public UserEntity ensureUser(String auth0Sub, String emailOrNull) {
    return repo.findByAuth0Sub(auth0Sub)
        .orElseGet(() -> repo.save(new UserEntity(auth0Sub)));
}
```

---

## Files That Need Updates

1. **UserEntity.java** - Add `auth0_sub` field, make email/password optional
2. **UserRepository.java** - Add method: `findByAuth0Sub(String auth0Sub)` (likely exists already)
3. **V4 Migration** - Already updated ✓
4. **EmailVerificationCodeRepository** - Already created ✓
5. **PasswordResetTokenRepository** - Already created ✓

---

## Question for You

Looking at the existing code, I need to know:

1. **Does UserRepository already have `findByAuth0Sub()` method?**
   - Check: `backend/src/main/java/com/erikallas/ndl/user/AppUserRepository.java`

2. **Is the existing UserEntity called `AppUserEntity` or `UserEntity`?**
   - The naming might be different

3. **Do you want to:**
   - A) Keep existing `AppUserEntity` and create new `AuthUserEntity` (separate tables)?
   - B) Update existing `AppUserEntity` to support both auth methods?

I recommend **option B** (update existing) because:

- ✅ One user table (simpler)
- ✅ All users have one `id`
- ✅ Auth methods are just different login paths
- ✅ Future: can support multiple auth methods per user

What do you think?
