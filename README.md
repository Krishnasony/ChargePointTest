# Fleet Charging Scheduler

A production-quality Android app for scheduling overnight charging of electric truck fleets. Built with **Clean Architecture**, **Kotlin**, **Jetpack Compose**, and **Hilt**.

## Overview

This app optimizes the charging schedule for a fleet of electric trucks using available chargers within a specified time horizon (e.g., 8 hours overnight). The goal is to maximize the number of trucks that can be fully charged.

## Features

- **Optimal Scheduling**: Uses a Greedy Shortest Job First algorithm to maximize the number of fully charged trucks
- **Modern UI**: Built with Jetpack Compose Material 3
- **Clean Architecture**: Clear separation of domain, data, and presentation layers
- **Dependency Injection**: Uses Hilt for DI
- **Reactive**: Kotlin Coroutines and StateFlow for async operations
- **Testable**: Comprehensive unit tests with MockK and Truth assertions

## Architecture

### Domain Layer
- **Entities**: `Truck`, `Charger`, `ScheduleResult`, `ScheduleAssignment`
- **Interfaces**: `ChargingScheduler`, `FleetRepository`
- **Use Cases**: `GenerateChargingScheduleUseCase`
- **Models**: Sealed `Result` and `AppError` types for error handling

### Data Layer
- **Repository Implementation**: `FleetRepositoryImpl`
- **Data Sources**: `LocalFleetDataSource` (currently in-memory, extensible to Room/API)

### Presentation Layer
- **ViewModel**: `ChargingScheduleViewModel` with StateFlow
- **UI**: Jetpack Compose screens with Material 3
- **State Management**: Sealed `ChargingScheduleUiState`

## Scheduling Algorithm

**Greedy Shortest Job First** approach:

1. For each truck, calculate minimum time to full charge across all chargers
2. Filter out trucks that cannot be fully charged within the time horizon
3. Sort remaining trucks by shortest charge time first
4. Assign each truck to the best available charger that can fit it

### Key Assumptions
- Each charger can charge only one truck at a time
- No preemption: once charging starts, it continues until complete
- No gaps: next truck starts immediately when previous completes
- Constant charging rate (ignoring EV charging curves)
- All chargers can charge any truck

## Project Structure

```
com.chargepoint.fleet/
├── domain/
│   ├── model/              # Domain entities and models
│   │   ├── Truck.kt
│   │   ├── Charger.kt
│   │   ├── ScheduleResult.kt
│   │   └── Result.kt
│   ├── repository/         # Repository interfaces
│   │   └── FleetRepository.kt
│   ├── scheduler/          # Scheduling algorithm
│   │   ├── ChargingScheduler.kt
│   │   └── GreedyShortestJobFirstScheduler.kt
│   └── usecase/            # Use cases
│       └── GenerateChargingScheduleUseCase.kt
├── data/
│   ├── repository/         # Repository implementations
│   │   └── FleetRepositoryImpl.kt
│   └── source/             # Data sources
│       ├── LocalFleetDataSource.kt
│       └── InMemoryFleetDataSource.kt
├── presentation/
│   ├── ChargingScheduleViewModel.kt
│   ├── ChargingScheduleScreen.kt
│   ├── MainActivity.kt
│   └── theme/
└── di/                     # Dependency injection modules
    ├── DataModule.kt
    └── DomainModule.kt
```

## Tech Stack

- **Language**: Kotlin 1.9.20
- **UI**: Jetpack Compose with Material 3
- **Async**: Kotlin Coroutines
- **DI**: Hilt 2.48
- **Architecture**: Clean Architecture with MVVM
- **Testing**: JUnit, MockK, Truth, Coroutines Test

## Build & Run

### Prerequisites
- Android Studio Hedgehog or later
- JDK 17
- Android SDK 34

### Build
```bash
./gradlew build
```

### Run Tests
```bash
./gradlew test
```

### Run App
1. Open project in Android Studio
2. Sync Gradle
3. Run on emulator or device (API 24+)

## Sample Data

The app includes sample data:
- **10 trucks** with varying battery capacities (180-220 kWh) and charge levels (5-50%)
- **3 chargers** with different power rates (50, 75, 100 kW)
- **8-hour** time horizon (typical overnight charging)

## Extensibility

The architecture supports easy extension:

### Add New Scheduling Algorithms
1. Implement `ChargingScheduler` interface
2. Bind in `DomainModule`
3. Use qualifiers for multiple implementations

### Add Remote Data Source
1. Create `RemoteFleetDataSource` interface
2. Implement with Retrofit/other HTTP client
3. Update `FleetRepositoryImpl` to use both local and remote sources

### Add Database Persistence
1. Implement `LocalFleetDataSource` with Room
2. Add DTOs and mappers
3. Update repository to handle offline scenarios

## Testing

Comprehensive unit tests included:
- `GreedyShortestJobFirstSchedulerTest`: Algorithm logic
- `GenerateChargingScheduleUseCaseTest`: Use case orchestration
- `TruckTest`: Domain model validation
- `ChargerTest`: Charging calculations

Run tests with:
```bash
./gradlew test
```

## Future Enhancements

- [ ] Real-time charging status updates
- [ ] Multiple scheduling algorithm comparison
- [ ] Cost optimization (electricity pricing)
- [ ] Battery health considerations
- [ ] Charging curve modeling
- [ ] Priority-based scheduling
- [ ] Historical data and analytics
- [ ] Export schedule to PDF/CSV

## License

Copyright 2025 ChargePoint Fleet Scheduler

## Author

Built with ❤️ using Clean Architecture principles and modern Android development practices.
