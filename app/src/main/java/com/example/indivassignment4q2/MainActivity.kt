package com.example.indivassignment4q2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.indivassignment4q2.ui.theme.IndivAssignment4Q2Theme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// We use a ViewModel to hold the app's data and state.
// This prevents the counter state from being cleared every time the screen rotates.
class CounterViewModel : ViewModel() {
    // Private mutable state flow to hold the counter value.
    private val _count = MutableStateFlow(0)

    // Publicly exposed as an immutable StateFlow for the UI to observe.
    val count = _count.asStateFlow()

    // We add another StateFlow to track the auto-increment mode.
    private val _isAutoIncrementing = MutableStateFlow(false)
    val isAutoIncrementing = _isAutoIncrementing.asStateFlow()

    // This holds a reference to our running coroutine, so we can cancel it later.
    private var autoIncrementJob: Job? = null

    fun increment() {
        _count.value++
    }

    fun decrement() {
        _count.value--
    }

    fun reset() {
        _count.value = 0
    }

    // This function handles the logic for turning auto-increment on and off.
    fun setAutoIncrement(enabled: Boolean) {
        if (_isAutoIncrementing.value == enabled) return // Avoid redundant work

        _isAutoIncrementing.value = enabled
        if (enabled) {
            // We launch a coroutine in the viewModelScope. This scope ensures the coroutine
            // is automatically cancelled when the ViewModel is cleared, preventing memory leaks.
            autoIncrementJob = viewModelScope.launch {
                while (true) {
                    delay(3000) // Requirement: wait 3 seconds.
                    increment()
                }
            }
        } else {
            // If the user toggles it off, we cancel the job to stop the loop.
            autoIncrementJob?.cancel()
        }
    }
}

class MainActivity : ComponentActivity() {
    // The `by viewModels()` delegate is the standard way to get a ViewModel
    // that is scoped to the Activity and survives configuration changes.
    private val viewModel: CounterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IndivAssignment4Q2Theme {
                // We replace the default Scaffold/Greeting with our main app composable.
                CounterApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun CounterApp(viewModel: CounterViewModel = viewModel()) {
    // `collectAsState` is how we connect our Compose UI to the ViewModel's data.
    // The UI will automatically update whenever the count value changes.
    val count by viewModel.count.collectAsState()
    // We also collect the auto-increment state to update the UI accordingly.
    val isAutoIncrementing by viewModel.isAutoIncrementing.collectAsState()

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Counter",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "$count",
                fontSize = 48.sp,
                fontWeight = FontWeight.Light
            )
            Spacer(modifier = Modifier.height(32.dp))
            Row {
                Button(onClick = { viewModel.decrement() }) {
                    Text("-1")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(onClick = { viewModel.increment() }) {
                    Text("+1")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { viewModel.reset() }) {
                Text("Reset")
            }
            Spacer(modifier = Modifier.height(32.dp))

            // This section adds the UI for the auto-increment feature.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(text = "Auto-Increment")
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = isAutoIncrementing,
                    // The switch directly calls the ViewModel to change the state.
                    onCheckedChange = { viewModel.setAutoIncrement(it) }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            // This text provides clear status feedback to the user.
            Text(text = "Auto mode: ${if (isAutoIncrementing) "ON" else "OFF"}")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CounterAppPreview() {
    IndivAssignment4Q2Theme {
        CounterApp()
    }
}
