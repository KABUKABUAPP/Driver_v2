# Kabukabu Driver App - Feature-Based Architecture

## Overview
The app has been reorganized into a **feature-based architecture** for better scalability, maintainability, and separation of concerns.

## Directory Structure

```
com.kabukabu.driver/
├── core/                           # Shared/Common modules
│   ├── theme/                      # App theme, colors, typography
│   ├── navigation/                 # Navigation routes and arguments
│   ├── utils/                      # Utility classes and helpers
│   └── data/
│       ├── local/                  # Local data (UserPreferences, etc.)
│       ├── remote/                 # API services and network layer
│       └── socket/                 # WebSocket services
│
├── features/                       # Feature modules
│   ├── auth/
│   │   ├── presentation/          # LoginScreen, OtpScreen, SplashScreen, ViewModels
│   │   └── data/                  # Login, Otp models
│   │
│   ├── home/
│   │   ├── presentation/          # HomeScreen, DriverViewModel, TripViewModel
│   │   └── data/                  # OnlineStatus, TripAction models
│   │
│   ├── wallet/
│   │   ├── presentation/          # WalletScreen, PaymentHistoryScreen, etc.
│   │   └── data/                  # DuePayment models
│   │
│   ├── support/
│   │   ├── presentation/          # SupportScreen, SupportDetailScreen, etc.
│   │   └── data/
│   │
│   ├── analytics/
│   │   ├── presentation/          # AnalyticsScreen, AnalyticsViewModel
│   │   └── data/                  # DriverAnalysis models
│   │
│   ├── trips/
│   │   ├── presentation/          # MyTripsScreen, TripsViewModel
│   │   └── data/                  # TripHistory models
│   │
│   ├── promotions/
│   │   ├── presentation/          # PromotionsScreen, PromotionsViewModel
│   │   └── data/
│   │
│   ├── profile/
│   │   ├── presentation/          # ProfileScreen
│   │   └── data/                  # Profile models
│   │
│   ├── about/
│   │   └── presentation/          # AboutScreen
│   │
│   └── repair_loan/
│       ├── presentation/          # RepairLoanScreen, RepairLoanViewModel
│       └── data/
│
├── MainActivity.kt                 # Main activity
├── KabukabuDriverApp.kt           # Application class
└── SplashActivity.kt              # Native splash activity
```

## Architecture Principles

### 1. **Feature-Based Organization**
Each feature is self-contained with its own:
- **presentation/**: UI components (Screens, ViewModels)
- **data/**: Data models and repositories specific to the feature
- **domain/**: Business logic (can be added as needed)

### 2. **Core Module**
Shared components used across multiple features:
- **theme/**: Material Design theme, colors, typography
- **navigation/**: Navigation routes and deep linking
- **utils/**: Common utilities, extensions, helpers
- **data/**: Shared data layer (API, database, preferences)

### 3. **Benefits**
- ✅ **Scalability**: Easy to add new features without affecting existing ones
- ✅ **Maintainability**: Clear boundaries between features
- ✅ **Testability**: Each feature can be tested independently
- ✅ **Team Collaboration**: Multiple developers can work on different features
- ✅ **Code Reusability**: Core module provides shared functionality

## Package Naming Convention

```kotlin
// Feature packages
com.kabukabu.driver.features.<feature_name>.presentation
com.kabukabu.driver.features.<feature_name>.data
com.kabukabu.driver.features.<feature_name>.domain

// Core packages
com.kabukabu.driver.core.<module_name>
```

## Import Examples

### Before (Old Structure)
```kotlin
import com.kabukabu.driver.ui.screens.LoginScreen
import com.kabukabu.driver.ui.viewmodels.LoginViewModel
import com.kabukabu.driver.data.model.Login
import com.kabukabu.driver.ui.theme.KabukabuDriverTheme
```

### After (New Structure)
```kotlin
import com.kabukabu.driver.features.auth.presentation.LoginScreen
import com.kabukabu.driver.features.auth.presentation.LoginViewModel
import com.kabukabu.driver.features.auth.data.Login
import com.kabukabu.driver.core.theme.KabukabuDriverTheme
```

## Features List

| Feature | Description | Screens |
|---------|-------------|---------|
| **auth** | Authentication & onboarding | Login, OTP, Splash |
| **home** | Main dashboard & trip management | Home, Map |
| **wallet** | Payment & wallet management | Wallet, Payment History, Sharp Payment |
| **support** | Customer support & tickets | Support, Support Detail, New Ticket |
| **analytics** | Driver analytics & insights | Analytics |
| **trips** | Trip history & details | My Trips |
| **promotions** | Promotions & offers | Promotions |
| **profile** | User profile management | Profile |
| **about** | App information | About |
| **repair_loan** | Repair loan feature | Repair Loan |

## Migration Notes

All imports have been automatically updated to reflect the new package structure. If you encounter any import errors:

1. Check the feature the class belongs to
2. Update the import to use the new package path
3. Ensure you're importing from `core` for shared components

## Future Enhancements

- Add **domain** layer for business logic in each feature
- Implement **use cases** for complex operations
- Add **repository** pattern for data access
- Consider **multi-module** setup for better build times

---

**Last Updated**: October 3, 2025
**Architecture Version**: 2.0
