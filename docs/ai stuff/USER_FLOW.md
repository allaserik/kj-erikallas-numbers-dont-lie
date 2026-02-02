┌─────────────────────────────────────────────────────┐
│ User visits app                                     │
└──────────────────────┬──────────────────────────────┘
                       │
                       ▼
            ┌──────────────────────┐
            │ Register             │
            │ Email + Password     │
            │ (validations)        │
            └──────────┬───────────┘
                       │
                       ▼
       ╔══════════════════════════════════╗
       ║ ✅ APP USABLE                    ║
       ║ Email sent with code (valid 24h) ║
       ║ User can use dashboard           ║
       ║ (with "incomplete" markers)      ║
       ╚════════════┬═════════════════════╝
                    │
        ┌───────────┴───────────┐
        │                       │
        ▼ (OPTIONAL)           ▼ (OPTIONAL)
    ┌─────────────┐        ┌─────────────────┐
    │ Verify      │        │ Enable 2FA      │
    │ Email Code  │        │ Authenticator   │
    │ (24hr)      │        │ or SMS          │
    │ [Resend]    │        │ (Recommended)   │
    └─────────────┘        └─────────────────┘
        │                       │
        └───────────┬───────────┘
                    │
        ┌───────────┴─────────────────────────┐
        │                                     │
        ▼ (IF INCOMPLETE)                     ▼
    ┌──────────────────────────┐      ┌──────────────────┐
    │ Complete Profile         │      │ Dashboard        │
    │ (Comprehensive data)     │      │ Shows:           │
    │ ├─ Basic info            │      │ ✓ Complete data  │
    │ ├─ Physical metrics      │      │ ✗ Missing data   │
    │ ├─ Activity/fitness      │      │    (disabled)    │
    │ ├─ Goals                 │      │ ✗ Email unverified
    │ ├─ Dietary prefs         │      │    (alert)       │
    │ └─ GDPR consent          │      │ ✗ 2FA not set    │
    │                          │      │    (recommended) │
    │ [Save Profile]           │      │                  │
    │ [Log Weight]             │      │ [Complete Setup] │
    │ [Skip - use later]       │      │ [Enable 2FA]     │
    └──────────────────────────┘      └──────────────────┘
        │
        └────────────────────────────────────┐
                                             │
                                             ▼
                               ┌──────────────────────┐
                               │ ✅ FULLY FEATURED    │
                               │ Dashboard            │
                               │ All features enabled │
                               └──────────────────────┘