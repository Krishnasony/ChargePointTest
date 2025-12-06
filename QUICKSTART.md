# Quick Start Guide

## Getting Started in 5 Minutes

### Step 1: Open Project
1. Launch Android Studio
2. File → Open
3. Navigate to `ChargePointTest` directory
4. Click OK

### Step 2: Sync Gradle
- Android Studio will automatically detect the project
- Wait for Gradle sync to complete
- If prompted, accept SDK installation

### Step 3: Run the App
1. Click the "Run" button (green play icon) or press `Ctrl+R` / `⌘+R`
2. Select an emulator or connected device
3. The app will build and launch

### Step 4: Explore the Schedule
- The app automatically generates a schedule on launch
- View:
  - Summary card with overall statistics
  - Each charger's schedule with assigned trucks
  - Unassigned trucks (if any)
- Click "Regenerate Schedule" to run again

## What You'll See

### Summary Card
- **Time Horizon**: 8 hours
- **Total Trucks**: 10 trucks in the fleet
- **Fully Charged**: Number that will be fully charged
- **Success Rate**: Percentage of trucks charged
- **Unassigned**: Trucks that can't fit in the time horizon

### Charger Schedules
Each charger shows:
- Charger ID and utilization percentage
- Total scheduled time
- List of assigned trucks with:
  - Truck ID
  - Start and end times
  - Duration of charging

### Unassigned Trucks
- Trucks requiring more time than available
- Shows their current charge level

## Understanding the Algorithm

The app uses a **Greedy Shortest Job First** strategy:

1. Calculates how long each truck needs to charge
2. Filters out trucks that can't fit in 8 hours
3. Sorts trucks by shortest charge time
4. Assigns each truck to the best available charger

This maximizes the number of fully charged trucks!

## Modifying Sample Data

To test with different data, edit `InMemoryFleetDataSource.kt`:

```kotlin
private val sampleTrucks = listOf(
    Truck(id = "TRUCK-001", batteryCapacityKWh = 200.0, currentChargePercent = 20.0),
    // Add or modify trucks here
)

private val sampleChargers = listOf(
    Charger(id = "CHARGER-A", rateKW = 50.0),
    // Add or modify chargers here
)

private val sampleTimeHorizon = 8 // Change time horizon
```

## Running Tests

In Android Studio:
1. Right-click on `app/src/test`
2. Select "Run Tests in 'fleet'"

Or from terminal:
```bash
./gradlew test
```

Test results will show algorithm correctness and edge case handling.

## Project Structure at a Glance

```
com.chargepoint.fleet/
├── domain/          # Business logic (pure Kotlin)
│   ├── model/       # Entities: Truck, Charger, Result
│   ├── scheduler/   # Scheduling algorithm
│   └── usecase/     # Use cases
├── data/            # Data layer
│   ├── repository/  # Repository implementation
│   └── source/      # Data sources (currently in-memory)
└── presentation/    # UI layer
    ├── ViewModel    # State management
    └── Screen       # Compose UI
```

## Common Tasks

### Change Algorithm Behavior
→ Edit `GreedyShortestJobFirstScheduler.kt`

### Modify UI
→ Edit `ChargingScheduleScreen.kt`

### Add New Data
→ Edit `InMemoryFleetDataSource.kt`

### Add Tests
→ Create test files in `app/src/test/`

## Need Help?

- Check `README.md` for full documentation
- See `TECHNICAL_DOCUMENTATION.md` for architecture details
- Review unit tests for usage examples

## Next Steps

1. ✅ Run the app and explore the UI
2. ✅ Run tests to see coverage
3. ✅ Modify sample data and observe changes
4. ✅ Read the technical documentation
5. ✅ Experiment with the scheduling algorithm

Happy coding! 🚀
