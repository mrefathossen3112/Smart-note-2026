package com.example.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.MedicalInformation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.VaultItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultItemDialog(
    initialItem: VaultItem? = null,
    onDismiss: () -> Unit,
    onSave: (title: String, category: String, docNumber: String, details: String, isImportant: Boolean) -> Unit
) {
    var title by remember { mutableStateOf(initialItem?.title ?: "") }
    var category by remember { mutableStateOf(initialItem?.category ?: "PASSPORT") }
    var docNumber by remember { mutableStateOf(initialItem?.documentNumber ?: "") }
    var details by remember { mutableStateOf(initialItem?.details ?: "") }
    var isImportant by remember { mutableStateOf(initialItem?.isImportant ?: true) }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    val categories = listOf(
        "PASSPORT" to "Passport",
        "VISA" to "Visa",
        "INSURANCE" to "Travel Insurance",
        "TICKET" to "Booking / Ticket",
        "EMERGENCY_CONTACT" to "Emergency ICE Contact",
        "MEDICAL" to "Medical & Allergies"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (initialItem == null) "Add Travel Document / Contact" else "Edit Document")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Category Picker
                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = categories.find { it.first == category }?.second ?: category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                    )
                    ExposedDropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        categories.forEach { (catKey, catLabel) ->
                            DropdownMenuItem(
                                text = { Text(catLabel) },
                                onClick = {
                                    category = catKey
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title (e.g. US Passport, Family Emergency)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vault_input_title"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = docNumber,
                    onValueChange = { docNumber = it },
                    label = { Text("Number / Phone / Policy ID") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vault_input_number"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("Details / Blood Group / Safe location") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isImportant,
                        onCheckedChange = { isImportant = it }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Pin to Top (Emergency Quick Access)", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(title, category, docNumber, details, isImportant)
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("vault_dialog_save_button")
            ) {
                Text("Save to Vault")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
