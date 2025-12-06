package com.chargepoint.fleet.domain.model

/**
 * Sealed class representing the result of an operation.
 * Used to encapsulate success or error states in a type-safe manner.
 */
sealed class Result<out T> {
    /**
     * Represents a successful operation with data.
     */
    data class Success<T>(val data: T) : Result<T>()

    /**
     * Represents a failed operation with an error.
     */
    data class Error(val error: AppError) : Result<Nothing>()

    /**
     * Returns true if this result is a success.
     */
    val isSuccess: Boolean
        get() = this is Success

    /**
     * Returns true if this result is an error.
     */
    val isError: Boolean
        get() = this is Error

    /**
     * Returns the data if this is a success, or null otherwise.
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }

    /**
     * Returns the error if this is an error, or null otherwise.
     */
    fun errorOrNull(): AppError? = when (this) {
        is Success -> null
        is Error -> error
    }
}

/**
 * Sealed class representing application-level errors.
 * Provides a structured way to handle different error types.
 */
sealed class AppError {
    /**
     * Unknown or unexpected error.
     */
    object Unknown : AppError()

    /**
     * Error related to data retrieval or validation.
     */
    data class DataError(val errorMessage: String) : AppError()

    /**
     * Error during calculation or scheduling.
     */
    data class CalculationError(val errorMessage: String) : AppError()

    /**
     * Error due to invalid input parameters.
     */
    data class InvalidInput(val errorMessage: String) : AppError()

    /**
     * Returns a human-readable error message.
     */
    fun getDisplayMessage(): String = when (this) {
        is Unknown -> "An unknown error occurred"
        is DataError -> "Data error: $errorMessage"
        is CalculationError -> "Calculation error: $errorMessage"
        is InvalidInput -> "Invalid input: $errorMessage"
    }
}
