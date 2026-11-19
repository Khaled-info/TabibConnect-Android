package com.example.tabibconnect.ui.screens.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tabibconnect.data.model.AppointmentStatus
import com.example.tabibconnect.data.repository.TabibRepository
import com.example.tabibconnect.ui.components.InitialsAvatar
import com.example.tabibconnect.ui.components.StatusBadge
import com.example.tabibconnect.ui.theme.PrimaryDarkTeal
import com.example.tabibconnect.ui.theme.PrimaryTeal
import com.example.tabibconnect.ui.theme.TealLightContainer
import com.example.tabibconnect.ui.theme.TealPrimary
import com.example.tabibconnect.ui.theme.TextDarkNavy

@Composable
fun PatientAppointmentsScreen(
    onNavigateToSearch: () -> Unit = {},
    onNavigateToCall: (String) -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: À venir, 1: Passés
    var appointmentToCancel by remember { mutableStateOf<com.example.tabibconnect.data.model.Appointment?>(null) }
    var selectedConsultationRecordForPdf by remember { mutableStateOf<com.example.tabibconnect.data.model.ConsultationRecord?>(null) }

    LaunchedEffect(Unit) {
        TabibRepository.fetchAppointmentsAsync()
        TabibRepository.fetchConsultationRecordsAsync()
    }

    val upcomingAppointments = TabibRepository.appointments.filter {
        it.status == AppointmentStatus.CONFIRMED || it.status == AppointmentStatus.PENDING
    }

    val pastAppointments = TabibRepository.appointments.filter {
        it.status == AppointmentStatus.COMPLETED || it.status == AppointmentStatus.CANCELLED || it.status == AppointmentStatus.REJECTED
    }

    val displayedList = if (selectedTab == 0) upcomingAppointments else pastAppointments

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAF9))
            .padding(16.dp)
    ) {
        Text(
            text = "Mes Rendez-vous",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = PrimaryDarkTeal
        )
        Text(
            text = "Gérez vos consultations médicales en cabinet",
            fontSize = 12.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = PrimaryDarkTeal
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("À venir (${upcomingAppointments.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Passés / Historique (${pastAppointments.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (displayedList.isEmpty()) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.EventBusy,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(52.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (selectedTab == 0) "Aucun rendez-vous à venir" else "Aucun rendez-vous passé dans l'historique",
                            color = TextDarkNavy,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Prenez rendez-vous avec un médecin spécialiste en cabinet.",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onNavigateToSearch,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Prendre un rendez-vous", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(displayedList) { apt ->
                    val cleanDocName = apt.doctorName.replace("Dr.", "", ignoreCase = true).replace("Dr", "", ignoreCase = true).trim()
                    val doctorInitials = cleanDocName.split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }.take(2).joinToString("").ifBlank { "DOC" }

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Header Row: Doctor Info + Status Badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    InitialsAvatar(
                                        initials = doctorInitials,
                                        backgroundColor = PrimaryTeal,
                                        textColor = Color.White,
                                        size = 46.dp,
                                        fontSize = 16.sp
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = apt.doctorName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = PrimaryDarkTeal
                                        )
                                        Text(
                                            text = apt.specialtyName,
                                            fontSize = 12.sp,
                                            color = Color.Gray,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                StatusBadge(status = apt.status)
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = Color(0xFFF1F5F9)
                            )

                            // Details Section
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = PrimaryTeal,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${apt.date} à ${apt.timeSlot}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = TextDarkNavy
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Bénéficiaire : ${apt.beneficiary}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF475569)
                                    )
                                }

                                if (apt.motif.isNotBlank()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Description,
                                            contentDescription = null,
                                            tint = Color(0xFF64748B),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Motif : ${apt.motif}",
                                            fontSize = 12.sp,
                                            color = Color(0xFF475569)
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocalHospital,
                                        contentDescription = null,
                                        tint = PrimaryTeal,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Consultation Présentielle en Cabinet",
                                        fontSize = 12.sp,
                                        color = PrimaryDarkTeal,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = Color(0xFFF1F5F9)
                            )

                            // Bottom Row: Price / Payment Info + Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = TealLightContainer,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Payments,
                                            contentDescription = null,
                                            tint = TealPrimary,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Tarif : ${apt.priceDt} DT • Paiement au cabinet",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TealPrimary
                                        )
                                    }
                                }

                                if (selectedTab == 0 && (apt.status == AppointmentStatus.CONFIRMED || apt.status == AppointmentStatus.PENDING)) {
                                    OutlinedButton(
                                        onClick = { appointmentToCancel = apt },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Annuler", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else if (apt.status == AppointmentStatus.COMPLETED) {
                                    Button(
                                        onClick = {
                                            val matchingRecord = TabibRepository.consultationRecords.firstOrNull { it.appointmentId == apt.id }
                                                ?: TabibRepository.consultationRecords.firstOrNull { it.patientName.equals(apt.patientName, ignoreCase = true) }
                                                ?: com.example.tabibconnect.data.model.ConsultationRecord(
                                                    appointmentId = apt.id,
                                                    doctorName = apt.doctorName,
                                                    patientName = apt.patientName,
                                                    specialtyName = apt.specialtyName,
                                                    date = apt.date,
                                                    diagnosis = "Consultation effectuée (${apt.motif})",
                                                    recommendations = "Traitement et recommandations du médecin."
                                                )
                                            selectedConsultationRecordForPdf = matchingRecord
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PictureAsPdf,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Résultat", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirmation Popup Modal Dialog for Appointment Cancellation
    if (appointmentToCancel != null) {
        val targetApt = appointmentToCancel!!
        AlertDialog(
            onDismissRequest = { appointmentToCancel = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(44.dp)
                )
            },
            title = {
                Text(
                    text = "Annuler le rendez-vous ?",
                    fontWeight = FontWeight.Bold,
                    color = TextDarkNavy,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Êtes-vous sûr de vouloir annuler votre rendez-vous avec ${targetApt.doctorName} prévu le ${targetApt.date} à ${targetApt.timeSlot} ?",
                    fontSize = 13.sp,
                    color = Color(0xFF475569)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        TabibRepository.cancelAppointment(targetApt.id)
                        appointmentToCancel = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Oui, annuler le RDV", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { appointmentToCancel = null },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Non, conserver le RDV", fontWeight = FontWeight.Bold, color = TextDarkNavy)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }

    if (selectedConsultationRecordForPdf != null) {
        com.example.tabibconnect.ui.components.ConsultationResultPdfDialog(
            record = selectedConsultationRecordForPdf!!,
            onDismiss = { selectedConsultationRecordForPdf = null }
        )
    }
}
