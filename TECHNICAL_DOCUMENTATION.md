# Fleet Charging Scheduler - Technical Documentation

## Project Overview

This Android application implements an optimal charging schedule for electric truck fleets using Clean Architecture principles, modern Android development practices, and Kotlin.

## Core Problem

**Goal**: Maximize the number of trucks fully charged within a given time horizon (e.g., 8 hours overnight).

**Constraints**:
- Each charger can charge only one truck at a time
- No preemption (once started, charging continues until complete)
- No gaps between charging sessions
- Constant charging rate (simplified model)

## Implementation Details

### Scheduling Algorithm: Greedy Shortest Job First (SJF)

#### Logic Flow:

1. **Calculate Best Times**
   ```kotlin
   for each truck:
       bestTime = min(timeToFull over all chargers)
   ```

2. **Filter by Horizon**
   ```kotlin
   eligibleTrucks = trucks.filter { bestTime <= timeHorizon }
   ```

3. **Sort by Shortest First**
   ```kotlin
   sortedTrucks = eligibleTrucks.sortBy { bestTime }
   ```

4. **Greedy Assignment**
   ```kotlin
   for each truck in sortedTrucks:
       bestCharger = charger with earliest completion time
       if (currentTime[bestCharger] + timeToFull <= horizon):
           assign truck to bestCharger
   ```

#### Time Complexity: O(n * m * log(n))
- n = number of trucks
- m = number of chargers
- Sorting dominates

#### Space Complexity: O(n + m)

### Energy Calculations

**Remaining Energy**:
```kotlin
remainingFraction = 1.0 - (currentChargePercent / 100.0)
remainingEnergyKWh = batteryCapacityKWh * remainingFraction
```

**Time to Full Charge**:
```kotlin
timeToFullHours = remainingEnergyKWh / chargerRateKW
```

**Example**:
- Truck: 200 kWh capacity, 25% charged
- Remaining: 200 * 0.75 = 150 kWh
- Charger: 75 kW
- Time: 150 / 75 = 2 hours

## Architecture Layers

### 1. Domain Layer (Pure Kotlin, No Android Dependencies)

**Purpose**: Business logic and entities

**Components**:
- `Truck`: Battery capacity, current charge
- `Charger`: Charging rate in kW
- `ScheduleResult`: Complete schedule with assignments
- `ChargingScheduler`: Interface for scheduling algorithms
- `FleetRepository`: Interface for data access
- `GenerateChargingScheduleUseCase`: Orchestrates scheduling

**Why Clean Architecture**:
- ✅ Testable without Android framework
- ✅ Framework-independent business logic
- ✅ Easy to swap implementations
- ✅ Single Responsibility Principle

### 2. Data Layer

**Purpose**: Data retrieval and storage

**Components**:
- `FleetRepositoryImpl`: Repository implementation
- `LocalFleetDataSource`: Interface for local data
- `InMemoryFleetDataSource`: Current implementation (extensible)

**Future Extensions**:
```kotlin
// Room Database
class RoomFleetDataSource @Inject constructor(
    private val fleetDao: FleetDao
) : LocalFleetDataSource {
    override suspend fun getTrucks(): List<Truck> {
        return fleetDao.getAllTrucks().map { it.toDomain() }
    }
}

// Remote API
class RemoteFleetDataSource @Inject constructor(
    private val api: FleetApi
) {
    suspend fun fetchTrucks(): List<Truck> {
        return api.getTrucks().map { it.toDomain() }
    }
}
```

### 3. Presentation Layer

**Purpose**: UI and user interaction

**Components**:
- `ChargingScheduleViewModel`: State management with StateFlow
- `ChargingScheduleScreen`: Jetpack Compose UI
- `ChargingScheduleUiState`: Sealed state representation

**State Management**:
```kotlin
sealed class ChargingScheduleUiState {
    object Initial : ChargingScheduleUiState()
    object Loading : ChargingScheduleUiState()
    data class Success(val schedule: ScheduleResult) : ChargingScheduleUiState()
    data class Error(val error: AppError) : ChargingScheduleUiState()
}
```

## Dependency Injection with Hilt

### Module Structure:

**DomainModule**: Provides scheduling algorithm
```kotlin
@Binds
@Singleton
abstract fun bindChargingScheduler(
    impl: GreedyShortestJobFirstScheduler
): ChargingScheduler
```

**DataModule**: Provides repository and data sources
```kotlin
@Binds
@Singleton
abstract fun bindFleetRepository(
    impl: FleetRepositoryImpl
): FleetRepository
```

### Benefits:
- ✅ Automatic lifecycle management
- ✅ Compile-time validation
- ✅ Easy testing with fakes/mocks
- ✅ Singleton management

## Error Handling

### Sealed Error Types:
```kotlin
sealed class AppError {
    object Unknown : AppError()
    data class DataError(val message: String) : AppError()
    data class CalculationError(val message: String) : AppError()
    data class InvalidInput(val message: String) : AppError()
}
```

### Result Wrapper:
```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val error: AppError) : Result<Nothing>()
}
```

### Usage in Use Case:
```kotlin
suspend operator fun invoke(): Result<ScheduleResult> {
    return try {
        val schedule = scheduler.schedule(...)
        Result.Success(schedule)
    } catch (e: IllegalArgumentException) {
        Result.Error(AppError.InvalidInput(e.message))
    } catch (e: Exception) {
        Result.Error(AppError.CalculationError(e.message))
    }
}
```

## Testing Strategy

### Unit Tests

**Domain Layer** (No mocking needed):
- `TruckTest`: Energy calculations, validation
- `ChargerTest`: Time calculations
- `GreedyShortestJobFirstSchedulerTest`: Algorithm correctness

**Use Case Layer** (MockK for dependencies):
- `GenerateChargingScheduleUseCaseTest`: Orchestration, error handling

### Test Coverage Goals:
- Domain logic: 100%
- Use cases: 90%+
- ViewModels: 80%+

### Sample Test Cases:

**Algorithm Tests**:
- ✅ Empty trucks list
- ✅ Single truck, single charger
- ✅ Shortest job first priority
- ✅ Multi-charger distribution
- ✅ Time horizon filtering
- ✅ Faster charger preference
- ✅ Sequential scheduling without gaps
- ✅ Complex scenarios

## Sample Data

### Default Fleet Configuration:

**Trucks** (10 total):
```kotlin
Truck("TRUCK-001", 200.0, 20.0)  // 160 kWh needed
Truck("TRUCK-002", 200.0, 50.0)  // 100 kWh needed
Truck("TRUCK-003", 200.0, 10.0)  // 180 kWh needed
// ... 7 more trucks
```

**Chargers** (3 total):
```kotlin
Charger("CHARGER-A", 50.0)   // Slow
Charger("CHARGER-B", 75.0)   // Medium
Charger("CHARGER-C", 100.0)  // Fast
```

**Time Horizon**: 8 hours (overnight)

### Expected Results:
- With optimal scheduling: 7-9 trucks fully charged
- Utilization: ~70-90% of available charger time
- Fast chargers get more assignments

## Performance Considerations

### Algorithm Performance:
- **Sorting**: O(n log n) - dominant operation
- **Assignment**: O(n * m) - tractable for realistic fleet sizes
- **Memory**: Linear in fleet size

### Scalability:
- ✅ Current: Handles 100s of trucks, 10s of chargers
- ⚠️ At 1000s of trucks: Consider optimizations:
  - Priority queue for assignments
  - Parallel computation with coroutines
  - Approximate algorithms (if exact solution not needed)

### Coroutine Usage:
```kotlin
// Computation on Default dispatcher (CPU-bound)
override suspend fun schedule(...) = withContext(Dispatchers.Default) {
    // Heavy computation here
}

// IO operations on IO dispatcher
suspend operator fun invoke() = withContext(Dispatchers.IO) {
    val trucks = repository.getTrucks()
    // ...
}
```

## Extension Points

### Adding New Algorithms:

1. **Create Implementation**:
```kotlin
class OptimalBranchAndBoundScheduler @Inject constructor() : ChargingScheduler {
    override suspend fun schedule(...): ScheduleResult {
        // Optimal but slower algorithm
    }
}
```

2. **Add Qualifier**:
```kotlin
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GreedyScheduler

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OptimalScheduler
```

3. **Bind Both**:
```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class DomainModule {
    @Binds @GreedyScheduler
    abstract fun bindGreedy(impl: GreedyShortestJobFirstScheduler): ChargingScheduler
    
    @Binds @OptimalScheduler
    abstract fun bindOptimal(impl: OptimalBranchAndBoundScheduler): ChargingScheduler
}
```

4. **Use in ViewModel**:
```kotlin
@HiltViewModel
class ChargingScheduleViewModel @Inject constructor(
    @GreedyScheduler private val greedyScheduler: ChargingScheduler,
    @OptimalScheduler private val optimalScheduler: ChargingScheduler
) : ViewModel() {
    // Switch between algorithms
}
```

### Adding Database Persistence:

1. **Add Room Dependency**
2. **Create Entities**:
```kotlin
@Entity(tableName = "trucks")
data class TruckEntity(
    @PrimaryKey val id: String,
    val batteryCapacityKWh: Double,
    val currentChargePercent: Double
)
```

3. **Create DAO**:
```kotlin
@Dao
interface FleetDao {
    @Query("SELECT * FROM trucks")
    suspend fun getAllTrucks(): List<TruckEntity>
}
```

4. **Implement DataSource**:
```kotlin
class RoomFleetDataSource @Inject constructor(
    private val dao: FleetDao
) : LocalFleetDataSource {
    override suspend fun getTrucks() = dao.getAllTrucks().map { it.toDomain() }
}
```

## Build & Deployment

### Gradle Configuration:
- **Kotlin**: 1.9.20
- **Compose Compiler**: 1.5.4
- **Hilt**: 2.48
- **Coroutines**: 1.7.3

### Build Variants:
- **Debug**: Development with logging
- **Release**: Optimized, ProGuard enabled

### CI/CD Ready:
```bash
# Run all tests
./gradlew test

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease
```

## Best Practices Demonstrated

1. ✅ **Clean Architecture**: Clear layer separation
2. ✅ **SOLID Principles**: SRP, OCP, DIP followed
3. ✅ **Testability**: Pure functions, dependency injection
4. ✅ **Type Safety**: Sealed classes, data classes
5. ✅ **Null Safety**: Kotlin's null safety features
6. ✅ **Immutability**: Data classes with val
7. ✅ **Coroutines**: Proper structured concurrency
8. ✅ **Error Handling**: Sealed error types
9. ✅ **Documentation**: KDoc comments throughout
10. ✅ **Modern UI**: Jetpack Compose with Material 3

## Conclusion

This project demonstrates production-quality Android development with:
- Robust architecture that scales
- Comprehensive testing strategy
- Modern Android development practices
- Clear separation of concerns
- Easy extensibility for future requirements

The codebase is ready for:
- Team collaboration
- Feature additions
- Algorithm experimentation
- Production deployment
