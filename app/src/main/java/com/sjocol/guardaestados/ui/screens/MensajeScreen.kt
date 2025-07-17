package com.sjocol.guardaestados.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.saveable.rememberSaveable
import com.sjocol.guardaestados.ui.components.FileActions
import android.widget.Toast
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.ui.res.stringResource
import com.sjocol.guardaestados.R
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.layout.ContentScale

// Lista básica de países con bandera emoji, nombre, prefijo y longitud mínima/máxima
val countryList = listOf(
    Country("🇦🇷", "Argentina", "+54", 10, 10),
    Country("🇧🇴", "Bolivia", "+591", 8, 8),
    Country("🇧🇷", "Brasil", "+55", 10, 11),
    Country("🇨🇱", "Chile", "+56", 9, 9),
    Country("🇨🇴", "Colombia", "+57", 10, 10),
    Country("🇨🇷", "Costa Rica", "+506", 8, 8),
    Country("🇨🇺", "Cuba", "+53", 8, 8),
    Country("🇩🇴", "Rep. Dominicana", "+1", 10, 10),
    Country("🇪🇨", "Ecuador", "+593", 9, 9),
    Country("🇸🇻", "El Salvador", "+503", 8, 8),
    Country("🇬🇹", "Guatemala", "+502", 8, 8),
    Country("🇭🇳", "Honduras", "+504", 8, 8),
    Country("🇲🇽", "México", "+52", 10, 10),
    Country("🇳🇮", "Nicaragua", "+505", 8, 8),
    Country("🇵🇦", "Panamá", "+507", 8, 8),
    Country("🇵🇾", "Paraguay", "+595", 9, 9),
    Country("🇵🇪", "Perú", "+51", 9, 9),
    Country("🇵🇷", "Puerto Rico", "+1", 10, 10),
    Country("🇺🇾", "Uruguay", "+598", 9, 9),
    Country("🇻🇪", "Venezuela", "+58", 10, 10),
    Country("🇺🇸", "Estados Unidos", "+1", 10, 10),
    Country("🇨🇦", "Canadá", "+1", 10, 10),
    Country("🇪🇸", "España", "+34", 9, 9),
    Country("🇫🇷", "Francia", "+33", 9, 9),
    Country("🇩🇪", "Alemania", "+49", 10, 11),
    Country("🇮🇹", "Italia", "+39", 10, 10),
    Country("🇬🇧", "Reino Unido", "+44", 10, 10),
    Country("🇵🇹", "Portugal", "+351", 9, 9),
    Country("🇳🇱", "Países Bajos", "+31", 9, 9),
    Country("🇧🇪", "Bélgica", "+32", 9, 9),
    Country("🇨🇭", "Suiza", "+41", 9, 9),
    Country("🇷🇺", "Rusia", "+7", 10, 10),
    Country("🇮🇳", "India", "+91", 10, 10),
    Country("🇨🇳", "China", "+86", 11, 11),
    Country("🇯🇵", "Japón", "+81", 10, 10),
    Country("🇦🇺", "Australia", "+61", 9, 9),
    Country("🇳🇿", "Nueva Zelanda", "+64", 9, 9),
    Country("🌍", "Otro", "", 6, 15)
)

data class Country(val flag: String, val name: String, val prefix: String, val minLength: Int, val maxLength: Int)

val CountrySaver = listSaver<Country, Any>(
    save = { listOf(it.flag, it.name, it.prefix, it.minLength, it.maxLength) },
    restore = {
        Country(
            flag = it[0] as String,
            name = it[1] as String,
            prefix = it[2] as String,
            minLength = it[3] as Int,
            maxLength = it[4] as Int
        )
    }
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MensajeScreen() {
    val context = LocalContext.current
    val whatsappErrorMsg = stringResource(R.string.whatsapp_error)
    var selectedCountry by rememberSaveable(stateSaver = CountrySaver) { mutableStateOf(countryList[0]) }
    var phoneNumber by rememberSaveable { mutableStateOf("") }
    var message by rememberSaveable { mutableStateOf("") }
    var showMessageDialog by remember { mutableStateOf(false) }
    val isValid = phoneNumber.length in selectedCountry.minLength..selectedCountry.maxLength && 
                  message.isNotBlank() && 
                  !phoneNumber.startsWith("0")
    val keyboardController = LocalSoftwareKeyboardController.current

    // Limpiar campos al volver a la pantalla
    LaunchedEffect(Unit) {
        phoneNumber = ""
        message = ""
        selectedCountry = countryList[0]
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Título
        Text(
            text = stringResource(R.string.send_message_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp),
            color = MaterialTheme.colorScheme.onBackground
        )
        
        // Grid de países
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(countryList) { country ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clickable {
                            selectedCountry = country
                            showMessageDialog = true
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (country == selectedCountry) 
                            MaterialTheme.colorScheme.primaryContainer 
                        else 
                            MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = country.flag,
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = country.name,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = country.prefix,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
    
    // Diálogo para enviar mensaje
    if (showMessageDialog) {
        AlertDialog(
            onDismissRequest = { showMessageDialog = false },
            title = { Text("${selectedCountry.flag} ${selectedCountry.name}") },
            text = {
                Column {
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it.filter { c -> c.isDigit() } },
                        label = { Text(stringResource(R.string.local_number)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = {
                            when {
                                phoneNumber.isNotEmpty() && phoneNumber.startsWith("0") ->
                                    Text(stringResource(R.string.number_starts_zero), color = MaterialTheme.colorScheme.error)
                                phoneNumber.isNotEmpty() && (phoneNumber.length < selectedCountry.minLength || phoneNumber.length > selectedCountry.maxLength) ->
                                    Text(stringResource(R.string.phone_length_error) + ": ${selectedCountry.minLength}-${selectedCountry.maxLength} " + stringResource(R.string.phone_digits), color = MaterialTheme.colorScheme.error)
                            }
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text(stringResource(R.string.message)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (isValid) {
                            try {
                                val fullNumber = selectedCountry.prefix.replace("+", "") + phoneNumber
                                FileActions.sendWhatsAppMessageToNumber(context, fullNumber, message)
                                showMessageDialog = false
                                phoneNumber = ""
                                message = ""
                            } catch (e: Exception) {
                                Toast.makeText(context, whatsappErrorMsg, Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    enabled = isValid
                ) {
                    Text(stringResource(R.string.send_whatsapp))
                }
            },
            dismissButton = {
                TextButton(onClick = { showMessageDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
} 