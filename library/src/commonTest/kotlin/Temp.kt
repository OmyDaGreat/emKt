package xyz.malefic.emkt

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
fun main() {
    MainScope().launch {
        val temperatureSignal = Signal<Double>()
        val humiditySignal = Signal<Int>()

        // Launch a coroutine to simulate temperature readings
        launch {
            repeat(10) {
                delay(500)
                temperatureSignal.emit(20.0 + Random.nextDouble() * 15)
            }
        }

        // Launch a coroutine to simulate humidity readings
        launch {
            repeat(10) {
                delay(700)
                humiditySignal.emit((40 + (Random.nextDouble() * 20).toInt()))
            }
        }

        // === REACTIVE FEATURES ===

        // 1. Filtering values
        launch {
            temperatureSignal.flow
                .filter { it > 25.0 }
                .collect { println("High temperature alert: $it°C") }
        }

        // 2. Transforming values
        launch {
            temperatureSignal.flow
                .map { celsius -> celsius * 9 / 5 + 32 }
                .collect { fahrenheit -> println("Temperature in Fahrenheit: $fahrenheit°F") }
        }

        // 3. Combining multiple flows
        launch {
            temperatureSignal.flow
                .combine(humiditySignal.flow) { temp, humidity ->
                    "Current conditions: $temp°C with $humidity% humidity"
                }.collect { println(it) }
        }

        // 4. Debouncing rapid emissions
        launch {
            temperatureSignal.flow
                .debounce(300.milliseconds)
                .collect { println("Debounced temperature: $it°C") }
        }

        // 5. Taking only the first few emissions
        launch {
            temperatureSignal.flow
                .take(3)
                .collect { println("Initial temperature reading: $it°C") }
        }

        // 6. State flow conversion to maintain latest value
        val latestTemperature =
            temperatureSignal.flow.stateIn(
                scope = this,
                started = SharingStarted.Eagerly,
                initialValue = 0.0,
            )
        println("Latest temperature state flow initialized with: ${latestTemperature.value}°C")

        // 7. Error handling
        launch {
            temperatureSignal.flow
                .map {
                    if (it > 30.0) {
                        throw Exception("Temperature too high!")
                    } else {
                        "Safe temperature: $it°C"
                    }
                }.catch { e -> println("Error caught: ${e.message}") }
                .collect { println(it) }
        }

        // 8. Flow completion handling
        launch {
            flow {
                repeat(3) {
                    emit(it)
                    delay(100)
                }
            }.onCompletion { println("Short flow completed!") }
                .collect { println("Short flow value: $it") }
        }

        delay(5000) // Let the program run for 5 seconds
    }
}
