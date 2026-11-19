package com.example.tabibconnect.ui.screens.teleconsultation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.tabibconnect.data.model.UserRole
import com.example.tabibconnect.data.repository.TabibRepository
import com.example.tabibconnect.ui.theme.EmergencyRed
import com.example.tabibconnect.ui.theme.PrimaryDarkTeal
import com.example.tabibconnect.ui.theme.PrimaryTeal

@Composable
fun VideoCallScreen(
    appointmentId: String,
    onCallEnded: () -> Unit
) {
    val appointment = TabibRepository.appointments.firstOrNull { it.id == appointmentId }
        ?: TabibRepository.appointments.first()

    var isMuted by remember { mutableStateOf(false) }
    var isCameraOn by remember { mutableStateOf(true) }
    var isAudioOnlyMode by remember { mutableStateOf(false) }

    var isChatOpen by remember { mutableStateOf(false) }
    var chatInputText by remember { mutableStateOf("") }

    var isReportModalOpen by remember { mutableStateOf(false) }
    var diagnosisInput by remember { mutableStateOf("") }
    var recommendationsInput by remember { mutableStateOf("") }
    var medication1Input by remember { mutableStateOf("") }
    var medication2Input by remember { mutableStateOf("") }

    val isDoctor = TabibRepository.currentUserRole.value == UserRole.DOCTOR

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E293B))
            .navigationBarsPadding()
            .imePadding()
    ) {
        // Video View Container or Audio Only Mode
        if (isAudioOnlyMode || !isCameraOn) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F172A)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(PrimaryTeal),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(72.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (isDoctor) appointment.patientName else appointment.doctorName,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Mode Audio Seul (Connexion faible 3G)",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            // Simulated Doctor/Patient Video Feed Container
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1E293B)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(100.dp)
                    )
                    Text(
                        text = "Flux Vidéo HD Chiffré (WebRTC)",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                    Text(
                        text = if (isDoctor) appointment.patientName else appointment.doctorName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                // Self PIP view overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 60.dp, end = 16.dp)
                        .size(100.dp, 140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Moi", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Top Status Bar Overlay
        Surface(
            color = Color.Black.copy(alpha = 0.5f),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = appointment.specialtyName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color.Green)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "00:12:45 • Réseau: Bon (300 kb/s)",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }
                }

                FilterChip(
                    selected = isAudioOnlyMode,
                    onClick = { isAudioOnlyMode = !isAudioOnlyMode },
                    label = { Text(if (isAudioOnlyMode) "Vidéo" else "Audio Seul", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryTeal,
                        selectedLabelColor = Color.White,
                        containerColor = Color.White.copy(alpha = 0.2f),
                        labelColor = Color.White
                    )
                )
            }
        }

        // Chat Drawer (when toggled)
        if (isChatOpen) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(bottom = 90.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Chat de Consultation", fontWeight = FontWeight.Bold, color = PrimaryDarkTeal)
                        IconButton(onClick = { isChatOpen = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Fermer")
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        items(TabibRepository.chatMessages) { msg ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = if (msg.isDoctor == isDoctor) Arrangement.End else Arrangement.Start
                            ) {
                                Surface(
                                    color = if (msg.isDoctor == isDoctor) PrimaryTeal else Color(0xFFE2E8F0),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = msg.text,
                                        color = if (msg.isDoctor == isDoctor) Color.White else Color.Black,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = chatInputText,
                            onValueChange = { chatInputText = it },
                            placeholder = { Text("Écrire un message...", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        IconButton(onClick = {
                            if (chatInputText.isNotBlank()) {
                                TabibRepository.addChatMessage(
                                    senderName = if (isDoctor) appointment.doctorName else appointment.patientName,
                                    isDoctor = isDoctor,
                                    text = chatInputText
                                )
                                chatInputText = ""
                            }
                        }) {
                            Icon(Icons.Default.Send, contentDescription = "Envoyer", tint = PrimaryTeal)
                        }
                    }
                }
            }
        }

        // Bottom Call Control Bar Toolbar
        Surface(
            color = Color.Black.copy(alpha = 0.8f),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            Row(
                modifier = Modifier
                    .padding(vertical = 16.dp, horizontal = 24.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Micro Toggle
                IconButton(
                    onClick = { isMuted = !isMuted },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isMuted) Color.Red else Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Micro",
                        tint = Color.White
                    )
                }

                // Camera Toggle
                IconButton(
                    onClick = { isCameraOn = !isCameraOn },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (!isCameraOn) Color.Red else Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = if (isCameraOn) Icons.Default.Videocam else Icons.Default.VideocamOff,
                        contentDescription = "Caméra",
                        tint = Color.White
                    )
                }

                // Chat Toggle
                IconButton(
                    onClick = { isChatOpen = !isChatOpen },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isChatOpen) PrimaryTeal else Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "Chat",
                        tint = Color.White
                    )
                }

                // End Call Button
                IconButton(
                    onClick = {
                        if (isDoctor) {
                            isReportModalOpen = true
                        } else {
                            onCallEnded()
                        }
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(EmergencyRed)
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "Raccrocher",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }

    // Doctor Consultation Ending Report & Prescription Creator Modal
    if (isReportModalOpen) {
        AlertDialog(
            onDismissRequest = { isReportModalOpen = false },
            title = { Text("Rédiger Compte Rendu & Ordonnance") },
            text = {
                Column {
                    OutlinedTextField(
                        value = diagnosisInput,
                        onValueChange = { diagnosisInput = it },
                        label = { Text("Diagnostic médical") },
                        placeholder = { Text("Ex: Arythmie cardiaque légère sous contrôle") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = recommendationsInput,
                        onValueChange = { recommendationsInput = it },
                        label = { Text("Recommandations & Suivi") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Ordonnance Médicamenteuse (Optionnel) :", fontWeight = FontWeight.Bold, fontSize = 12.sp)

                    OutlinedTextField(
                        value = medication1Input,
                        onValueChange = { medication1Input = it },
                        placeholder = { Text("Médicament 1: Ex: CORDARONE 200mg") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = medication2Input,
                        onValueChange = { medication2Input = it },
                        placeholder = { Text("Médicament 2: Ex: MAGNE B6") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val meds = listOfNotNull(
                            medication1Input.ifBlank { null },
                            medication2Input.ifBlank { null }
                        )
                        TabibRepository.addConsultationRecord(
                            appointmentId = appointment.id,
                            doctorName = appointment.doctorName,
                            patientName = appointment.patientName,
                            specialtyName = appointment.specialtyName,
                            diagnosis = if (diagnosisInput.isBlank()) "Téléconsultation de contrôle effectuée" else diagnosisInput,
                            recommendations = if (recommendationsInput.isBlank()) "Continuer le traitement habituel" else recommendationsInput,
                            medications = meds
                        )
                        isReportModalOpen = false
                        onCallEnded()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                ) {
                    Text("Enregistrer & Terminer")
                }
            },
            dismissButton = {
                TextButton(onClick = { isReportModalOpen = false }) { Text("Annuler") }
            }
        )
    }
}
