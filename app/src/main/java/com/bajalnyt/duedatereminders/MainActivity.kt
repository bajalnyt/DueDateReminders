package com.bajalnyt.duedatereminders

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bajalnyt.duedatereminders.data.AppDatabase
import com.bajalnyt.duedatereminders.data.DueItem
import com.bajalnyt.duedatereminders.ui.theme.DueDateRemindersTheme
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import androidx.compose.ui.graphics.Color
import java.time.temporal.ChronoUnit
import androidx.compose.foundation.background

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DueDateRemindersTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DueItemForm()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DueItemForm() {
    var name by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = remember { AppDatabase.getDatabase(context) }
    
    val items by database.dueItemDao().getAllItems().collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Item Name") },
            modifier = Modifier.fillMaxWidth()
        )
        
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = { showDatePicker = true }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)),
                    style = MaterialTheme.typography.bodyLarge
                )
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Select date"
                )
            }
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(
                    state = rememberDatePickerState(
                        initialSelectedDateMillis = selectedDate
                            .toEpochDay() * 24 * 60 * 60 * 1000
                    ),
                    showModeToggle = false,
//                    onDateChange = { millis ->
//                        millis?.let {
//                            selectedDate = LocalDate.ofEpochDay(it / (24 * 60 * 60 * 1000))
//                        }
//                    }
                )
            }
        }

        Button(
            onClick = {
                scope.launch {
                    try {
                        val dueItem = DueItem(
                            name = name,
                            dueDate = selectedDate
                        )
                        database.dueItemDao().insert(dueItem)
                        name = ""
                    } catch (e: Exception) {
                        // Handle error
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = name.isNotBlank()
        ) {
            Text("Add Item")
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items) { item ->
                DueItemCard(
                    item = item,
                    onDelete = {
                        scope.launch {
                            database.dueItemDao().delete(item)
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DueItemCard(
    item: DueItem,
    onDelete: () -> Unit
) {
    val daysUntilDue = ChronoUnit.DAYS.between(LocalDate.now(), item.dueDate)
    val backgroundColor = when {
        daysUntilDue <= 60 -> Color(0xFFF55B72) // Red for <= 60 days
        daysUntilDue <= 100 -> Color(0xFFFAF2A0) // Yellow for <= 100 days
        else -> MaterialTheme.colorScheme.surface // Default surface color
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (daysUntilDue <= 60) Color.White else Color.Unspecified
                )
                Text(
                    text = item.dueDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (daysUntilDue <= 60) Color.White else Color.Unspecified
                )
            }
            
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete item",
                    tint = if (daysUntilDue <= 60) Color.White else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}