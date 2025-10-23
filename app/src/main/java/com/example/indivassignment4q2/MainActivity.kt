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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.indivassignment4q2.ui.theme.IndivAssignment4Q2Theme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// We use a ViewModel to hold the app's data and state.
// This prevents the counter state from being cleared every time the screen rotates.
class CounterViewModel : ViewModel() {
    // Private mutable state flow to hold the counter value.
    private val _count = MutableStateFlow(0)

    // Publicly exposed as an immutable StateFlow for the UI to observe.
    val count = _count.asStateFlow()

    fun increment() {
        _count.value++
    }

    fun decrement() {
        _count.value--
    }

    fun reset() {
        _count.value = 0
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
