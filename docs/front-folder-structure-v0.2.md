```bash
src/
  app/              // composition and routes
    App.tsx
    routes.tsx
    layout/
      AppShell.tsx  // header + nav + content
  features/         // “vertical slices” (recommended)
    dashboard/
      DashboardPage.tsx
      components/
      api.ts
      types.ts
    setup/
      SetupWizardPage.tsx
      steps/
    profile/
    goals/
    weight/
    insights/
  shared/
    api/
      client.ts
    auth/
      useAuthToken.ts
    ui/
      Button.tsx
      Card.tsx
      Alert.tsx
```
