package com.example.ui.screens.tools

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.CurrencyConverter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyConverterSection(
    isOnline: Boolean,
    isRefreshing: Boolean,
    onRefreshRates: () -> Unit
) {
    val currencies = remember { CurrencyConverter.getAvailableCurrencies() }
    var fromCode by remember { mutableStateOf("USD") }
    var toCode by remember { mutableStateOf("BDT") }
    var amountInput by remember { mutableStateOf("100") }

    var fromDropdownExpanded by remember { mutableStateOf(false) }
    var toDropdownExpanded by remember { mutableStateOf(false) }

    val amountDouble = amountInput.toDoubleOrNull() ?: 0.0
    val convertedAmount = CurrencyConverter.convert(amountDouble, fromCode, toCode)
    val fromSymbol = CurrencyConverter.getSymbol(fromCode)
    val toSymbol = CurrencyConverter.getSymbol(toCode)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Conversion Card
        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("currency_conversion_card"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Offline Currency Calculator",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (isOnline) {
                            IconButton(
                                onClick = onRefreshRates,
                                enabled = !isRefreshing,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh Live Rates", modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Text(
                        text = if (isOnline) "Live exchange rates synced with Open ER-API" else "Running 100% offline with cached baseline rates",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // From & To selectors with swap
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // From Dropdown
                        ExposedDropdownMenuBox(
                            expanded = fromDropdownExpanded,
                            onExpandedChange = { fromDropdownExpanded = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = fromCode,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("From") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fromDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                            )
                            ExposedDropdownMenu(
                                expanded = fromDropdownExpanded,
                                onDismissRequest = { fromDropdownExpanded = false }
                            ) {
                                currencies.forEach { c ->
                                    DropdownMenuItem(
                                        text = { Text("${c.code} (${c.symbol}) - ${c.name}") },
                                        onClick = {
                                            fromCode = c.code
                                            fromDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = {
                                val temp = fromCode
                                fromCode = toCode
                                toCode = temp
                            },
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .testTag("swap_currency_button")
                        ) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = "Swap", tint = MaterialTheme.colorScheme.primary)
                        }

                        // To Dropdown
                        ExposedDropdownMenuBox(
                            expanded = toDropdownExpanded,
                            onExpandedChange = { toDropdownExpanded = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = toCode,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("To") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = toDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                            )
                            ExposedDropdownMenu(
                                expanded = toDropdownExpanded,
                                onDismissRequest = { toDropdownExpanded = false }
                            ) {
                                currencies.forEach { c ->
                                    DropdownMenuItem(
                                        text = { Text("${c.code} (${c.symbol}) - ${c.name}") },
                                        onClick = {
                                            toCode = c.code
                                            toDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Amount input
                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it },
                        label = { Text("Amount ($fromCode)") },
                        leadingIcon = { Text(fromSymbol, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("currency_amount_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick amount chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("10", "50", "100", "500", "1000").forEach { quick ->
                            AssistChip(
                                onClick = { amountInput = quick },
                                label = { Text("+$quick") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Result Display Box
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "$fromSymbol$amountInput $fromCode =",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$toSymbol${String.format("%,.2f", convertedAmount)} $toCode",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val singleUnit = CurrencyConverter.convert(1.0, fromCode, toCode)
                            Text(
                                text = "1 $fromCode = ${String.format("%.4f", singleUnit)} $toCode",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }

        // Quick Travel Cheat Sheet
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Traveler's Quick Reference ($fromCode ⇄ $toCode)",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val increments = listOf(1.0, 5.0, 10.0, 20.0, 50.0, 100.0, 500.0)
                    increments.forEach { amt ->
                        val conv = CurrencyConverter.convert(amt, fromCode, toCode)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("$fromSymbol${amt.toInt()} $fromCode", fontWeight = FontWeight.Medium)
                            Text("$toSymbol${String.format("%,.2f", conv)} $toCode", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}
