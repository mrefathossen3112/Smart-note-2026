package com.example.ui.dialogs

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.Trip
import com.example.util.DateUtils
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDialog(
    initialTrip: Trip? = null,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        destination: String,
        startDate: Long,
        endDate: Long,
        budget: Double,
        notes: String,
        category: String,
        colorHex: String
    ) -> Unit
) {
    val context = LocalContext.current
    val now = Calendar.getInstance()

    var title by remember { mutableStateOf(initialTrip?.title ?: "") }
    var destination by remember { mutableStateOf(initialTrip?.destination ?: "") }
    var startDate by remember {
        mutableStateOf(initialTrip?.startDate ?: now.timeInMillis)
    }
    var endDate by remember {
        mutableStateOf(initialTrip?.endDate ?: (now.timeInMillis + 7 * 86400000L))
    }
    var budgetText by remember {
        mutableStateOf(if ((initialTrip?.budget ?: 0.0) > 0.0) initialTrip?.budget.toString() else "")
    }
    var notes by remember { mutableStateOf(initialTrip?.notes ?: "") }
    var category by remember { mutableStateOf(initialTrip?.category ?: "Vacation") }
    var selectedColor by remember { mutableStateOf(initialTrip?.colorHex ?: "#0D9488") }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    val categories = listOf("Vacation", "Business", "Adventure", "Roadtrip", "Family", "Solo", "Weekend")

    val colorOptions = listOf(
        "#0D9488", "#2563EB", "#7C3AED", "#DB2777", "#EA580C", "#16A34A"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (initialTrip == null) "New Trip" else "Edit Trip",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = destination,
                    onValueChange = { destination = it },
                    label = { Text("Destination *") },
                    placeholder = { Text("e.g. Kyoto, Japan") },
                    leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("trip_destination_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Trip Title") },
                    placeholder = { Text("e.g. Autumn Adventure") },
                    leadingIcon = { Icon(Icons.Default.FlightTakeoff, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("trip_title_input"),
                    singleLine = true
                )

                // Dates selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val c = Calendar.getInstance().apply { timeInMillis = startDate }
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    val newStart = Calendar.getInstance().apply {
                                        set(y, m, d, 9, 0)
                                    }.timeInMillis
                                    startDate = newStart
                                    if (endDate < newStart) {
                                        endDate = newStart + 86400000L
                                    }
                                },
                                c.get(Calendar.YEAR),
                                c.get(Calendar.MONTH),
                                c.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text("Start", style = MaterialTheme.typography.labelSmall)
                            Text(DateUtils.formatMonthDay(startDate), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            val c = Calendar.getInstance().apply { timeInMillis = endDate }
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    val newEnd = Calendar.getInstance().apply {
                                        set(y, m, d, 18, 0)
                                    }.timeInMillis
                                    endDate = if (newEnd >= startDate) newEnd else startDate
                                },
                                c.get(Calendar.YEAR),
                                c.get(Calendar.MONTH),
                                c.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text("End", style = MaterialTheme.typography.labelSmall)
                            Text(DateUtils.formatMonthDay(endDate), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Category dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Trip Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Estimated Budget
                OutlinedTextField(
                    value = budgetText,
                    onValueChange = { budgetText = it },
                    label = { Text("Estimated Budget ($)") },
                    placeholder = { Text("e.g. 1500") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Color accent
                Text("Theme Color", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    colorOptions.forEach { hex ->
                        val color = Color(android.graphics.Color.parseColor(hex))
                        val isSelected = selectedColor.equals(hex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { selectedColor = hex }
                                .then(
                                    if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                    else Modifier
                                )
                        )
                    }
                }

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Highlights") },
                    placeholder = { Text("Key reservations, flight notes, reminders...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp),
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (destination.isNotBlank()) {
                        val finalTitle = title.ifBlank { "Trip to $destination" }
                        val budget = budgetText.toDoubleOrNull() ?: 0.0
                        onSave(finalTitle, destination, startDate, endDate, budget, notes, category, selectedColor)
                    }
                },
                enabled = destination.isNotBlank(),
                modifier = Modifier.testTag("save_trip_button")
            ) {
                Text("Save Trip")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
