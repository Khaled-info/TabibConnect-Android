package com.example.tabibconnect.ui.screens.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tabibconnect.data.repository.TabibRepository
import com.example.tabibconnect.ui.theme.LightTealContainer
import com.example.tabibconnect.ui.theme.PrimaryDarkTeal
import com.example.tabibconnect.ui.theme.PrimaryTeal
import com.example.tabibconnect.ui.theme.TealLightContainer
import com.example.tabibconnect.ui.theme.TealPrimary
import com.example.tabibconnect.ui.theme.TextDarkNavy
import kotlinx.coroutines.launch

@Composable
fun PatientProfileScreen(
    onLogout: () -> Unit
) {
    val patient = TabibRepository.currentPatient.value
    var isAddRelativeOpen by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    // Fetch relatives from backend on launch
    LaunchedEffect(Unit) {
        val currentId = TabibRepository.currentPatient.value.id
        val currentUserId = TabibRepository.currentPatient.value.userId
        TabibRepository.fetchRelativesAsync(currentId, currentUserId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAF9))
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "Mon Espace Santé",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = PrimaryDarkTeal
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Profile Header Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(LightTealContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = PrimaryTeal,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = patient.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = PrimaryDarkTeal
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = TealPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = patient.phone,
                                fontSize = 13.sp,
                                color = TextDarkNavy,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = TealPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${patient.governorate}, ${patient.delegation}",
                                fontSize = 12.sp,
                                color = TextDarkNavy,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Medical & Personal Info Summary
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LightTealContainer, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Sexe", fontSize = 11.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(patient.gender, fontWeight = FontWeight.Bold, color = PrimaryDarkTeal, fontSize = 13.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Date de naissance", fontSize = 11.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(patient.birthDate, fontWeight = FontWeight.Bold, color = PrimaryDarkTeal, fontSize = 13.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Proches", fontSize = 11.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("${patient.relatives.size} rattaché(s)", fontWeight = FontWeight.Bold, color = PrimaryDarkTeal, fontSize = 13.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Relatives Section Header & Add Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Proches rattachés (Famille)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryDarkTeal
                )
                Text(
                    text = "Gérez la liste de vos proches pour les réservations",
                    fontSize = 11.sp,
                    color = Color(0xFF6B7C85)
                )
            }

            Button(
                onClick = { isAddRelativeOpen = true },
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Ajouter", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (patient.relatives.isEmpty()) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.FamilyRestroom,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Aucun proche rattaché pour le moment.",
                        fontSize = 13.sp,
                        color = Color(0xFF6B7C85),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            patient.relatives.forEach { rel ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(TealLightContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.FamilyRestroom, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(rel.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDarkNavy)
                            Text("Lien : ${rel.relation}", fontSize = 12.sp, color = Color(0xFF6B7C85))
                        }
                        Surface(
                            color = TealLightContainer,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = rel.relation,
                                color = TealPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Language & Settings Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = PrimaryTeal)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Langue de l'application", fontWeight = FontWeight.Medium)
                    }

                    TextButton(onClick = {
                        TabibRepository.currentLanguage.value =
                            if (TabibRepository.currentLanguage.value == "FR") "AR" else "FR"
                    }) {
                        Text(
                            text = if (TabibRepository.currentLanguage.value == "FR") "Français (FR)" else "العربية (AR)",
                            fontWeight = FontWeight.Bold,
                            color = PrimaryDarkTeal
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = PrimaryTeal)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Exporter mes données de santé", fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Logout Button
        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFCE8E6), contentColor = Color(0xFFE53935)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Se déconnecter", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }

    // Modern Modal Interface for "Ajouter un proche"
    if (isAddRelativeOpen) {
        AddRelativeDialog(
            onDismiss = { isAddRelativeOpen = false },
            onSuccess = { isAddRelativeOpen = false }
        )
    }
}

// Dedicated Modern Form Dialog matching "Mot de passe oublié ?" Popup Style
@Composable
fun AddRelativeDialog(
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var relativeName by remember { mutableStateOf("") }
    var selectedRelation by remember { mutableStateOf("Enfant") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val relationOptions = listOf("Enfant", "Parent", "Conjoint", "Frère / Sœur", "Autre")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = null,
                        tint = TealPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Ajouter un Proche",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDarkNavy
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Rattachez un membre de votre famille à votre compte",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7C85)
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (errorMessage != null) {
                    Surface(
                        color = Color(0xFFFEF2F2),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Error, contentDescription = null, tint = Color.Red, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(errorMessage!!, fontSize = 12.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Text(
                    text = "Nom et Prénom du proche",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDarkNavy
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = relativeName,
                    onValueChange = {
                        relativeName = it
                        errorMessage = null
                    },
                    placeholder = { Text("Ex: Youssef Ben Ammor", fontSize = 13.sp, color = Color(0xFF94A3B8)) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TealPrimary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = TealPrimary,
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Lien de parenté",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDarkNavy
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Interactive Selection Pills
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    relationOptions.chunked(3).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowItems.forEach { rel ->
                                val isSelected = rel == selectedRelation
                                Surface(
                                    onClick = { selectedRelation = rel },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) TealLightContainer else Color.White,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) TealPrimary else Color(0xFFE2E8F0)
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = rel,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) TealPrimary else TextDarkNavy
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !isSubmitting,
                onClick = {
                    if (relativeName.isBlank()) {
                        errorMessage = "Veuillez saisir le nom et prénom."
                        return@Button
                    }

                    isSubmitting = true
                    coroutineScope.launch {
                        val res = TabibRepository.addRelativeAsync(relativeName, selectedRelation)
                        isSubmitting = false
                        if (res.isSuccess) {
                            onSuccess()
                        } else {
                            errorMessage = res.exceptionOrNull()?.message ?: "Erreur d'enregistrement"
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Enregistrer le proche", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Annuler", color = Color(0xFF6B7C85), fontWeight = FontWeight.SemiBold)
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White
    )
}
