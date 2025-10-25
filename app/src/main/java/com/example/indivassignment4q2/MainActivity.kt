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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
    private val _count = MutableStateFlow(0)
    val count = _count.asStateFlow()

    private val _isAutoIncrementing = MutableStateFlow(false)
    val isAutoIncrementing = _isAutoIncrementing.asStateFlow()

    // State for the auto-increment interval, defaulting to 3 seconds.
    private val _autoIncrementInterval = MutableStateFlow(3000L)
    val autoIncrementInterval = _autoIncrementInterval.asStateFlow()

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

    // Updates the interval and restarts the auto-increment job if it's active.
    fun setInterval(newInterval: Long) {
        _autoIncrementInterval.value = newInterval
        if (_isAutoIncrementing.value) {
            autoIncrementJob?.cancel()
            startAutoIncrementJob()
        }
    }

    fun setAutoIncrement(enabled: Boolean) {
        if (_isAutoIncrementing.value == enabled) return

        _isAutoIncrementing.value = enabled
        if (enabled) {
            startAutoIncrementJob()
        } else {
            autoIncrementJob?.cancel()
        }
    }

    // Extracted the job creation logic to avoid code duplication.
    private fun startAutoIncrementJob() {
        autoIncrementJob = viewModelScope.launch {
            while (true) {
                delay(autoIncrementInterval.value)
                increment()
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    private val viewModel: CounterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IndivAssignment4Q2Theme {
                CounterApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CounterApp(viewModel: CounterViewModel = viewModel()) {
    // This state controls whether we show the main screen or the settings screen.
    var showSettings by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (showSettings) "Settings" else "Counter++") },
                actions = {
                    if (!showSettings) {
                        IconButton(onClick = { showSettings = true }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (showSettings) {
            val interval by viewModel.autoIncrementInterval.collectAsState()
            SettingsScreen(
                modifier = Modifier.padding(paddingValues),
                currentInterval = interval,
                onIntervalChange = { viewModel.setInterval(it) },
                onDone = { showSettings = false } // Callback to go back.
            )
        } else {
            CounterScreen(
                modifier = Modifier.padding(paddingValues),
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun CounterScreen(modifier: Modifier = Modifier, viewModel: CounterViewModel) {
    val count by viewModel.count.collectAsState()
    val isAutoIncrementing by viewModel.isAutoIncrementing.collectAsState()

    Column(
        modifier = modifier.fillMaxSize(),
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(text = "Auto-Increment")
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = isAutoIncrementing,
                onCheckedChange = { viewModel.setAutoIncrement(it) }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Auto mode: ${if (isAutoIncrementing) "ON" else "OFF"}")
    }
}

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    currentInterval: Long,
    onIntervalChange: (Long) -> Unit,
    onDone: () -> Unit
) {
    // This local state tracks the slider's position while the user is dragging it.
    var sliderPosition by remember { mutableFloatStateOf(currentInterval / 1000f) }

    // This ensures the slider resets its position if the external state changes.
    LaunchedEffect(currentInterval) {
        sliderPosition = currentInterval / 1000f
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Auto-Increment Interval", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        Text("%.1f seconds".format(sliderPosition))
        Slider(
            value = sliderPosition,
            onValueChange = { sliderPosition = it },
            // To be efficient, we only update the ViewModel when the user finishes dragging.
            onValueChangeFinished = {
                onIntervalChange((sliderPosition * 1000).toLong())
            },
            valueRange = 1f..10f,
            steps = 8 // This creates 9 steps for 10 discrete values (1.0, 2.0, ... 10.0)
        )
        Spacer(Modifier.height(32.dp))
        Button(onClick = onDone) {
            Text("Done")
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

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    IndivAssignment4Q2Theme {
        SettingsScreen(currentInterval = 3000L, onIntervalChange = {}, onDone = {})
    }
}
