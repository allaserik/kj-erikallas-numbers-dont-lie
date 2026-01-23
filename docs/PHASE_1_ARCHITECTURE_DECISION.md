# Phase 1 - Architecture Decision: How to Integrate Email/Password Auth

**Current Situation**:

- Existing: `AppUserEntity` (for Auth0 users, has `auth0_sub`)
- New: We need email/password support

**Decision Point**: How do we structure this?

---

## Option A: Extend AppUserEntity (Recommended)

**Approach**: Update the existing `AppUserEntity` to support BOTH auth methods

```java
// AppUserEntity becomes multi-purpose
public class AppUserEntity {
    // Auth0 users use:
    private String auth0Sub;          // "google-oauth2|123"

    // Email/password users use:
    private String email;             // "erik@example.com"
    private String passwordHash;      // "bcrypt$2a$12$..."
    private Boolean emailVerified;    // false → true after code
    private OffsetDateTime updatedAt; // Add this field
}
```

**Pros**:

- ✅ One user table, one entity
- ✅ All users have one `id`
- ✅ Existing code not broken
- ✅ Simpler queries
- ✅ Can support Auth0 + Email/Password + Future OAuth

**Cons**:

- Each user has "unused" fields (Auth0 users have null email/password_hash)
- Slightly more complex validation logic

**Result**:

- Auth0 users: `auth0Sub="..."`, `email=null`, `passwordHash=null`, `emailVerified=true`
- Email users: `auth0Sub=null`, `email="..."`, `passwordHash="..."`, `emailVerified=false→true`

---

## Option B: Separate Entities (Not Recommended)

```
UserEntity (email/password)
  ├── id, email, password_hash, email_verified

AppUserEntity (Auth0)
  ├── id, auth0_sub, email
```

**Pros**:

- Clear separation
- No unused fields

**Cons**:

- ❌ Two user tables (complicates foreign keys)
- ❌ Each user has two different `id` values (confusion)
- ❌ Health profile/goals linked to which user?
- ❌ Existing code breaks

---

## My Recommendation: **Option A**

Update `AppUserEntity` to be a unified user model that supports multiple auth methods.

### How It Works

```
AppUserEntity extends to support:
├── Auth0 Users (existing, no change to them):
│   ├── auth0Sub: "google-oauth2|123"
│   ├── email: null (optional from Auth0)
│   ├── passwordHash: null
│   └── emailVerified: true
│
└── Email/Password Users (new):
    ├── auth0Sub: null
    ├── email: "erik@example.com"
    ├── passwordHash: "bcrypt_hash"
    └── emailVerified: false (until code verified)
```

### Changes Needed

1. **Update AppUserEntity**:
   - Keep: `id`, `auth0Sub`, `email`, `createdAt`
   - Add: `passwordHash`, `emailVerified`, `updatedAt`
   - Change: `auth0Sub` from `nullable = false` to `nullable = true`
   - Add helper methods: `isAuth0User()`, `isEmailUser()`

2. **Update AppUserRepository**:
   - Keep: `findByAuth0Sub()`
   - Add: `findByEmailIgnoreCase()`
   - Add: `existsByEmailIgnoreCase()`

3. **Update V4 Migration**:
   - Alter table, don't create new one (already done ✓)

4. **Create Auth Services** (next step):
   - `EmailAuthService` (for email/password registration/login)
   - Reuse `AppUserRepository` for all user access

---

## Questions for Confirmation

Before I update the code:

1. **Does this approach make sense to you?**
2. **Should Auth0 users be migrated to have an `email` value when they first login?**
   - Yes: Store email from Auth0 claims
   - No: Keep it null
3. **Can we change `auth0Sub` from `nullable = false` to `nullable = true`?**
   - This allows email-only users

---

## Implementation Path

If you agree with Option A:

1. ✅ Update `AppUserEntity` to support both auth methods
2. ✅ Update `AppUserRepository` with email queries
3. ✅ Update V4 migration to ALTER table (already done ✓)
4. Create `EmailAuthService` using `AppUserEntity` and `AppUserRepository`
5. Create password encoding configuration
6. Create `AuthController` with email/password endpoints

This way, **all existing code continues to work**, and we **add email/password support alongside Auth0**.

---

## Your Decision

Say one of:

- **"Option A"** - Update AppUserEntity to support both (recommended)
- **"Option B"** - Create separate entities (not recommended)
- **"Ask questions"** - Clarify something first
